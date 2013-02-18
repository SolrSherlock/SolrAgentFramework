/**
 * 
 */
package org.topicquests.agent.mock;

import java.util.*;
import org.semispace.Tuple;
import org.semispace.api.ISemiSpace;
import org.semispace.api.ISemiSpaceTuple;
import org.semispace.api.ITupleFields;
import org.semispace.api.ITupleTags;
import org.topicquests.agent.solr.AgentEnvironment;
import org.topicquests.common.ResultPojo;
import org.topicquests.common.api.IResult;
import org.topicquests.solr.SolrEnvironment;
import org.topicquests.tuplespace.TupleFactory;
import org.topicquests.agent.api.IPluggableAgent;
import org.topicquests.util.LoggingPlatform;
import org.topicquests.util.Tracer;

/**
 * @author park
 * <p>This is just a "mock" agent which will be given various simple tasks
 * to exercise until we have the platform developed.</p>
 * <p>A useful first task is to reach into the Solr
 */
public class MockAgent implements IPluggableAgent {
	private LoggingPlatform log;
	private Tracer tracer;
	private AgentEnvironment agentEnvironment;
	private SolrEnvironment solrEnvironment;
	private ISemiSpace blackboard;
	private Worker thread;
	private TupleFactory factory; // not actually used here
	/**
	 * <code>agentName</code> is used both in the internal blackboard,
	 * and the remote one
	 */
	private String agentName;

	/* (non-Javadoc)
	 * @see org.topicquests.solr.api.IPluggableAgent#init(org.topicquests.solr.SolrEnvironment)
	 */
	@Override
	public IResult init(AgentEnvironment env, String agentName) {
		this.agentName = agentName;
		agentEnvironment = env;
		solrEnvironment = env.getSolrEnvironment();
		blackboard = agentEnvironment.getTupleSpaceEnvironment().getTupleSpace();
		factory = new TupleFactory(agentEnvironment.getTupleSpaceEnvironment());
		log  = LoggingPlatform.getInstance();
		tracer = log.getTracer(agentName);
		IResult result = new ResultPojo();
		thread = new Worker();
		return result;
	}
	
	class Worker extends Thread {
		private boolean isRunning = true;
		private Object synchObject = new Object();
		private ISemiSpaceTuple template;
		
		public void shutDown() {
			synchronized(synchObject) {
				isRunning = false;
				synchObject.notify();
			}
		}
		
		Worker() {
			template = new Tuple(1, ITupleTags.NEW_SOLR_DOC);
			template.set(ITupleFields.AGENT_NAME, agentName);
			this.start();
		}
		
		public void run() {
			log.logDebug( "MockAgent.Worker started");
			ISemiSpaceTuple t=null;
			String cargo;
			while(isRunning) {
				t = blackboard.read(template, 1000); // leave up to a second
				System.out.println("MockAgent read "+t+" "+isRunning);
				if (t == null) {
					synchronized(synchObject) {
						try {
							synchObject.wait(1000);
						} catch (Exception e) {}
					}
				}
				if (isRunning && t != null) {
					log.logDebug("MockAgent GOT"+ t.getJSON());
					if (containsData(t)) {
						cargo = (String)t.get(ITupleFields.CARGO);
						tracer.trace(0, cargo);
						System.out.println(cargo);
					}
					t = null;
				}
			}
		}
	}
	
	/**
	 * {"id":1361127537959,"tag":"NewSolrDoc","cargo":"nodata"}
	 * is an example of a returned tuple with "no data"
	 * @param t
	 * @return
	 */
	boolean containsData(ISemiSpaceTuple t) {
		String cargo = (String)t.get(ITupleFields.CARGO);
		if (cargo.equals("nodata"))
			return false;
		return true;
	}
	
	@Override
	public void shutDown() {
		thread.shutDown();
		tracer.shutDown();
		log.shutDown();	
	}

}
