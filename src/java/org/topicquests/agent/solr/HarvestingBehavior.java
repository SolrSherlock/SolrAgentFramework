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

import org.semispace.Tuple;
import org.semispace.api.ISemiSpace;
import org.semispace.api.ISemiSpaceTuple;
import org.semispace.api.ITupleFields;
import org.semispace.api.ITupleTags;
import org.topicquests.common.api.IResult;
import org.topicquests.tuplespace.TupleFactory;
import org.topicquests.tuplespace.api.ITupleSpaceConnectorListener;
import org.topicquests.tcp.TupleSpaceConnector;

/**
 * @author park
 * <p>This behavior keeps the local blackboard filled with fresh
 * documents from Solr</p>
 */
public class HarvestingBehavior implements ITupleSpaceConnectorListener {
	private AgentEnvironment environment;
	private TupleSpaceConnector connector;
	private ISemiSpace blackboard;
	private boolean isRunning = true;
	private Object synchObject = new Object();
	private TupleFactory factory;
	private String agentName;
	
	/**
	 * @param e
	 * @param agentName
	 */
	public HarvestingBehavior(AgentEnvironment e, String agentName) {
		environment = e;
		this.agentName = agentName;
		factory = new TupleFactory(environment.getTupleSpaceEnvironment());
		connector = environment.getTupleSpaceConnector();
		blackboard = environment.getTupleSpaceEnvironment().getTupleSpace();
		connector.readTuple(ITupleTags.NEW_SOLR_DOC, agentName, this);
	}
	
	public void shutDown() {
		isRunning = false;
	}
	

	@Override
	public void acceptResult(IResult result) {
		if (!isRunning)
			return;
		environment.logDebug("HarvestingBehavior.acceptResult "+result.getResultObject());
		boolean mustWait = true;
		ISemiSpaceTuple t;
		if (result != null) { //TODO can't happen
			if (result.hasError())
				environment.logError(result.getErrorString(), null);
			String cargo = (String)result.getResultObject();
			if (cargo != null) {
				if (!containsData(cargo)) {
					t = factory.newTuple(ITupleTags.NEW_SOLR_DOC);
					t.set(ITupleFields.CARGO, cargo);
					environment.logDebug("HarvestingBehavior.acceptResult writing "+t.getJSON());
					blackboard.write(t, 3000);
					mustWait = false;
				}
			}
		}
		if (!isRunning)
			return;
		if (mustWait) {
			//nothing found
			synchronized(synchObject) {
				try {
				synchObject.wait(1000); // wait for a while
				} catch (Exception e) {System.out.println("Oops1");}
			}
		} else {
			synchronized(synchObject) {
				try {
				synchObject.wait(500); // don't hog the serial port
				} catch (Exception e) {System.out.println("Oops2");}
			}
		}
		if (!isRunning)
			return;
		//call again
		connector.readTuple(ITupleTags.NEW_SOLR_DOC, agentName, this);
	}
	/**
	 * {"id":1361127537959,"tag":"NewSolrDoc","cargo":"nodata"}
	 * is an example of a returned tuple with "no data"
	 * @param t <code>true</code> if that representation of nodata is found
	 * @return
	 */
	boolean containsData(String cargo) {
		if (cargo.contains("\"cargo\":\"nodata\""))
			return true;
		return false;
	}

}
