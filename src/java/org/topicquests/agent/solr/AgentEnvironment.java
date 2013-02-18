/*
 * Copyright 2013, TopicQuests
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.topicquests.agent.solr;

import java.util.*;

import org.nex.config.ConfigPullParser;
import org.topicquests.tcp.TupleSpaceConnector;
import org.topicquests.tuplespace.TupleSpaceEnvironment;
import org.topicquests.util.LoggingPlatform;
import org.topicquests.solr.SolrEnvironment;
import org.topicquests.agent.api.IAgentFrameworkBehavior;
import org.topicquests.agent.api.IPluggableAgent;

/**
 * @author park
 * <p>AgentEnvironment is the <em>boot</em> environment for this platform</p>
 * <p>It creates TupleSpaceEnvironment and SolrEnvironment</p>
 * <p>SolrEnviornment concludes booting by loading all listed agents</p>
 */
public class AgentEnvironment {
	private LoggingPlatform log = LoggingPlatform.getInstance();
	private Hashtable<String,Object>props;
	private TupleSpaceConnector connector;
	private int connectorport = 0;
	private String connectorserver = "";
	private SolrEnvironment solrEnvironment;
	private TupleSpaceEnvironment tuplespaceEnvironment;
	private String _AgentName;
	private String _AgentBehavior;
	private Map<String,IPluggableAgent> agents;
	private HarvestingBehavior harvester = null;

	/**
	 * 
	 */
	public AgentEnvironment() {
		ConfigPullParser p = new ConfigPullParser("config-props.xml");
		props = p.getProperties();
		_AgentName = (String)props.get("AgentName");
		_AgentBehavior = (String)props.get("AgentBehavior");
		agents = new HashMap<String,IPluggableAgent>();
		try {
			String portx = (String)props.get("TuplespacePort");
			connectorport = Integer.parseInt(portx);
			connectorserver = (String)props.get("TuplespaceServer");
			solrEnvironment = new SolrEnvironment(props);
			tuplespaceEnvironment = new TupleSpaceEnvironment(props);
			connector = new TupleSpaceConnector(this, connectorserver,connectorport);
			//now bring in the agents
			//once they are in, they go right to work.
			bootAgents();
			//this fires up the harvesting behavior
			if (_AgentBehavior.equals(IAgentFrameworkBehavior.HARVEST_BEHAVIOR))
				harvester = new HarvestingBehavior(this, _AgentName);
		} catch (Exception e) {
			e.printStackTrace();
			logError(e.getMessage(),e);
			throw new RuntimeException(e);
		}	
		AddShutdownHook hook = new AddShutdownHook();
		hook.attachShutDownHook();
		record("AgentEnvironment running");
		logDebug("AgentEnvironment running");
		System.out.println("AgentEnvironment running");
	}
	
	private void bootAgents() throws Exception {
	   	List<List<String>> dbs = (List<List<String>>)props.get("Agents");
    	if (dbs != null && dbs.size() > 0) {
    		String cp, key;
    		int len = dbs.size();
    		
    		for (int i=0;i<len;i++) {
    			cp = ((String)((List<String>)dbs.get(i)).get(1)).trim();
    			key = ((String)((List<String>)dbs.get(i)).get(0)).trim();
    			logDebug("SolrEnvironment.bootAgents "+key + " | "+cp);
				Class o = Class.forName(cp);
				IPluggableAgent a = (IPluggableAgent)o.newInstance();
    			a.init(this, key);
    			agents.put(key, a);
    		}
    	}		
	}
	
	public Hashtable<String,Object> getProperties() {
		return this.props;
	}
	public TupleSpaceConnector getTupleSpaceConnector() {
		return this.connector;
	}
	
	public TupleSpaceEnvironment getTupleSpaceEnvironment() {
		return this.tuplespaceEnvironment;
	}

	public SolrEnvironment getSolrEnvironment() {
		return this.solrEnvironment;
	}
	public void shutDown() {
		connector.shutDown();
		log.shutDown();
		//shut down all the agents
		Iterator<String>itr = agents.keySet().iterator();
		while (itr.hasNext())
			agents.get(itr.next()).shutDown();
		solrEnvironment.shutDown();
		if (harvester != null)
			harvester.shutDown();
	}
	
	public class AddShutdownHook{
		 public void attachShutDownHook(){
		  Runtime.getRuntime().addShutdownHook(new Thread() {
		   @Override
		   public void run() {
			   shutDown();
		    System.out.println("Inside Add Shutdown Hook");
		   }
		  });
		  System.out.println("Shut Down Hook Attached.");
		 }
	}

	/////////////////////////////
	// Utilities
	public void logDebug(String msg) {
		log.logDebug(msg);
	}
	
	public void logError(String msg, Exception e) {
		log.logError(msg,e);
	}
	
	public void record(String msg) {
		log.record(msg);
	}

}
