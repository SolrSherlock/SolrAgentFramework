/**
 * 
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
