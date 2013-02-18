/**
 * 
 */
package tests;

import org.topicquests.agent.solr.AgentEnvironment;
import org.topicquests.common.api.IResult;
import org.topicquests.model.api.INode;
import org.topicquests.model.api.INodeModel;
import org.topicquests.solr.SolrEnvironment;
import org.topicquests.solr.api.ISolrDataProvider;

/**
 * @author park
 *
 */
public class MockTestOne {
	private AgentEnvironment agentEnvironment;
	private SolrEnvironment solrEnvironment;
	
	/**
	 * Do not try to run this if SolrAgentFramework is already running
	 */
	public MockTestOne() {
		agentEnvironment = new AgentEnvironment();
		solrEnvironment = agentEnvironment.getSolrEnvironment();
		try {
			runTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//agentEnvironment.shutDown();
	}
	
	void runTest() throws Exception {
		System.out.println("Test Starting");
		ISolrDataProvider p = solrEnvironment.getDataProvider();
		INodeModel m = p.getNodeModel();
		//IResult newNode(String label, String description, String lang, String userId, String smallImagePath, String largeImagePath, boolean isPrivate);
		IResult r = m.newNode("My Label", "Just a node to test", "en", "admin", null, null, false);
		INode node= (INode)r.getResultObject();
		p.putNode(node);
		//that should start things off!
		//System.out.println("Test Ending");
	}

}
