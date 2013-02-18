/**
 * 
 */
package org.topicquests.agent.api;

import org.topicquests.agent.solr.AgentEnvironment;
import org.topicquests.common.api.IResult;

/**
 * @author park
 *
 */
public interface IPluggableAgent {

	/**
	 * Initialize the agent by accepting an instance of
	 * {@link AgentEnvironment}, performing initializations,
	 * and returning error messages, if any.
	 * @param env
	 * @param agentName
	 * @return
	 */
	IResult init(AgentEnvironment env, String agentName);
	
	/**
	 * Necessary for any services the agents might need
	 */
	void shutDown();
}
