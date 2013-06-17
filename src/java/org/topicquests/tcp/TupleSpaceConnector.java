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
package org.topicquests.tcp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
//import net.sf.json.JSONObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.topicquests.agent.solr.AgentEnvironment;
import org.topicquests.common.ResultPojo;
import org.topicquests.common.api.IResult;
import org.topicquests.tuplespace.api.ITupleSpaceConnectorListener;
import org.semispace.api.IConstants;
import org.semispace.api.ITupleFields;

/**
 * @author park
 * <p>A system to communicate with SolrAgentCoordinator</p>
 * <p>We communicate using JSON strings which include a command, and
 * some cargo</p>
 */
public class TupleSpaceConnector {
	private AgentEnvironment environment;
	private String server = "";
	private int port = 0;
	private ServerSocket srvr;
	private Worker worker;
	private Object synchObject = new Object();
	private JSONParser parser;

	/**
	 * 
	 */
	public TupleSpaceConnector(AgentEnvironment env, String server, int port) throws Exception {
		environment = env;
		this.server = server;
		this.port = port;
		parser = new JSONParser();
		System.out.println("TupleSpaceConnector starting "+port);
        srvr = new ServerSocket(port);
        worker = new Worker();
	}
	
	class Worker extends Thread {
		private List<String>messages;
		private List<ITupleSpaceConnectorListener>listeners;
		private boolean isRunning = true;
		
		public Worker() {
			messages = new ArrayList<String>();
			listeners = new ArrayList<ITupleSpaceConnectorListener>();
			isRunning = true;
			this.start();
		}
		
		public void addTask(String json, ITupleSpaceConnectorListener listener) {
			synchronized(messages) {
				messages.add(json);
				listeners.add(listener);
				messages.notifyAll();
//				System.out.println("TASKS "+messages+" "+listeners);
			}
		}
		
		public void shutDown() {
			synchronized(messages) {
				isRunning = false;
				messages.notifyAll();
			}
		}
		
		public void run() {
			String theMsg = null;
			ITupleSpaceConnectorListener theListener = null;
			while (isRunning) {
				synchronized(messages) {
					if (messages.isEmpty()) {
						theMsg = null;
						theListener = null;
						try {
							messages.wait();
						} catch (Exception e) {}
					} else if (isRunning && !messages.isEmpty()) {
						theMsg = messages.remove(0);
						theListener = listeners.remove(0);
//						System.out.println("XXX "+theMsg+" "+theListener);
					}
				}
				if (isRunning && theMsg != null) {
					sendMessage(theMsg,theListener);
					theMsg = null;
					theListener = null;
				}
			}
			
		}
	}
	/**
	 * <p>This version of <em>read</em> does not wait for available objects;
	 * it can return <code>null</code>, meaning it must be run inside a
	 * threaded loop</p>
	 * @param tag
	 * @param agentName
	 * @param listener
	 */
	public void readTuple(String tag, String agentName, ITupleSpaceConnectorListener listener) {
		String json = getJSONString(IConstants.READ, tag, "", agentName);
//		System.out.println("TupleSpaceConnector.readTuple "+tag+" "+agentName);
		worker.addTask(json, listener);
	}
	
	public void takeTuple(String tag,  String cargo, ITupleSpaceConnectorListener listener) {
		String json = getJSONString(IConstants.TAKE, tag, cargo,null);
		worker.addTask(json, listener);
	}

	public void insertTuple(String tag, String cargo, ITupleSpaceConnectorListener listener) {
		String json = getJSONString(IConstants.PUT, tag, cargo, null);
		worker.addTask(json, listener);
	}
	
	String getJSONString(String command, String tag, String cargo, String agentName) {
		Map<String,Object> m = new HashMap<String,Object>();
		m.put(ITupleFields.COMMAND, command);
		m.put(ITupleFields.TAG, tag);
		m.put(ITupleFields.CARGO, cargo);
		if (agentName != null)
			m.put(ITupleFields.AGENT_NAME, agentName);
		JSONObject j = new JSONObject(m);
		return j.toString();
	}
	
	/**
	 * Returns field "cargo" 
	 * @param json
	 * @param listener
	 */
	void sendMessage(String json, ITupleSpaceConnectorListener listener) {
		IResult result = new ResultPojo();
		try {
//			System.out.println("Server "+port);
	        Socket skt = srvr.accept();
//	        System.out.println("Server has connected! ");
	        PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
//	        System.out.println("Sending string: " + json);
	        out.print(json);
	        out.flush();
//	        System.out.println("SS-1");
	     //   InputStream is = skt.getInputStream();
//	        System.out.println("SS-2");
	        StringBuilder buf = new StringBuilder();
	        String line;
	        InputStream is = skt.getInputStream();
//	        System.out.println("SS-2a "+skt.isInputShutdown()); //false
	        BufferedReader in = new BufferedReader(new
	        			InputStreamReader(is));
//	        System.out.println("SS-3 "+in.ready()); //false
	        while (!in.ready()) {  // stuck here
//	        	System.out.print(".");
	        	synchronized(synchObject) {
	        		try {
	        			synchObject.wait(50);
	        		} catch (Exception e) {}
	        	}
	        }
	        int x;
			while ((x = in.read()) > -1)
				buf.append((char)x);
			in.close();	        
	        out.close();
//	        System.out.println("SS-4 "+buf);

//			skt.close();
	        line = buf.toString();
	        environment.logDebug("TupleSpaceConnector got back "+line);
			JSONObject jobj = (JSONObject)parser.parse(line);
			//what we get back IS the cargo from the original tuple
			result.setResultObject(jobj.toString());
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
			result.addErrorString(e.getMessage());
		}
		listener.acceptResult(result);
	}
	
	public void shutDown() {
		if (worker != null)
			worker.shutDown();
		worker = null;
		try {
			srvr.close();
		} catch (Exception e) {
			environment.logError(e.getMessage(), e);
		}
		srvr = null;
	}
	protected void finalize() throws Throwable {
		shutDown();
	}

}
