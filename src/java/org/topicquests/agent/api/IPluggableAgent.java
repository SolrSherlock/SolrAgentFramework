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
