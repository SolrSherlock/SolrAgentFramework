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
package tests;

import org.topicquests.agent.solr.AgentEnvironment;
import org.topicquests.common.api.IResult;
import org.topicquests.tuplespace.api.ITupleSpaceConnectorListener;
import org.topicquests.tcp.TupleSpaceConnector;

/**
 * @author park
 *
 */
public class AgentClientTest implements ITupleSpaceConnectorListener {
	AgentEnvironment environment;
	TupleSpaceConnector connector;
	/**
	 * 
	 */
	public AgentClientTest() {
		environment = new AgentEnvironment();
		connector = environment.getTupleSpaceConnector();
		try {
			runTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void runTest() throws Exception {
		String tag = "MyTag";
		String cargo = "{\"name\":\"Joe Smith\"}";
		System.out.println("SENDING: "+cargo);
		connector.insertTuple(tag, cargo, this);
		connector.readTuple(tag,"fooy", this);
	}

	@Override
	public void acceptResult(IResult result) {
		System.out.println("GOT: "+result.getErrorString()+" | "+result.getResultObject());		
	}
}
