/**
 * 
 */
package org.topicquests.tuplespace.api;

import org.semispace.api.ITupleTags;

/**
 * @author park
 * <p>A collection of known and expected agent tags</p>
 */
public interface IBlackboardTags extends ITupleTags {

	public static final String
		/** 
		 * <p>TellAskAgent tags which serve as <em>core</em>
		 *  for establishment of communication, then get
		 *  extended with a userId (or agentId) when a conversation
		 *  starts.</p>
		 */
		TELL_TASK				= "telltask",
		TELL_RESPONSE			= "tellresponse",
		ASK_TASK				= "asktask",
		ASK_RESPONSE			= "askresponse",
		/** Concordance Agent */
		CONCORDANCE_TASK		= "concordancetask",
		CONCORDANCE_RESPONSE	= "concordanceresponse",
		/** TupleFramework */
		TUPLE_TASK				= "tupletask",
		TUPLE_RESPONSE			= "tupleresponse",
		/** WordNet */
		WORDNET_TASK			= "wordnettask",
		WORDNET_RESPONSE		= "wordnetresponse",
		/** OpenNLP */
		OPENNLP_TASK			= "opennlptask",
		OPENNLP_RESPONSE		= "opennlpresponse",
		/** Gate */
		GATE_TASK				= "gatetask",
		GATE_RESPONSE			= "gateresponse",
		/** ReVerb */
		REVERB_TASK				= "reverbtask",
		REVERB_RESPONSE			= "reverbresponse",
		/** OpenCYC */
		CYC_TASK				= "cyctask",
		CYC_RESPONSE			= "cycresponse";
	
		
}
