package org.sapia.ubik.mcast.control;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Clock;

public class TestChannelCallback implements ChannelCallback {
	
	class NodeRegistration {
		long lastHeartbeatTime;
		TestChannelCallback node;
		
		NodeRegistration(TestChannelCallback node) {
			this.node = node;
		}
	}
	
	private String 												node;
  private TestCallbackAddress           address 		 = new TestCallbackAddress();	
	private EventChannelController 	controller;
	private Map<String, NodeRegistration> siblings 		 = new ConcurrentHashMap<String, NodeRegistration>();
	private Map<String, NodeRegistration> deadSiblings = new ConcurrentHashMap<String, NodeRegistration>();
  private volatile boolean              down;
  private volatile Set<String>          forceResync;
	
	public TestChannelCallback(String node, Clock clock, ControllerConfiguration config) {
		this.node = node;
		this.controller = new EventChannelController(clock, config, this);
  }
	
	public TestChannelCallback addSibling(TestChannelCallback sibling) {
		if(!this.node.equals(sibling.getNode())) {
			siblings.put(sibling.getNode(), new NodeRegistration(sibling));
		}
		return this;
	}
	
	public boolean containsSibling(String node) {
		return siblings.containsKey(node);
	}
	
	public void flagDown() {
	  down = true;
	}
	
	public void flagUp() {
		down = false;
	}
	
	public boolean isForceResyncCalled() {
	  return forceResync != null;
	}
	
	public Set<String> getTargetedForceResyncNodes() {
	  return forceResync;
	}
	
	@Override
	public void resync() {
	}
	
	@Override
	public void down(String node) {
		deadSiblings.put(node, siblings.remove(node));
	}
	
	@Override
	public String getNode() {
	  return node;
	}
	
	@Override
	public ServerAddress getAddress() {
	  return address;
	}
	
	@Override
	public Set<String> getNodes() {
	  return new TreeSet<String>(siblings.keySet());
	}
	
	@Override
	public void heartbeat(String node, ServerAddress address) {
	  siblings.get(node).lastHeartbeatTime = this.controller.getContext().getClock().currentTimeMillis();
	}
	
	@Override
	public void sendNotification(ControlNotification notif) {
		if(!down) {
  	  notif.getTargetedNodes().remove(node);
      if(!notif.getTargetedNodes().isEmpty()) {
      	String targeted = notif.getTargetedNodes().iterator().next();
      	notif.getTargetedNodes().remove(targeted);
      	TestChannelCallback callback = getCallback(targeted);
      	if(callback == null) {
      		throw new IllegalArgumentException("No node for: " + targeted);
      	}
      	callback.getController().onNotification(getNode(), notif);
      }
		}
	}
	
	@Override
	public void sendRequest(ControlRequest req) {
		if(!down) {
  	  req.getTargetedNodes().remove(node);
  	  if(!req.getTargetedNodes().isEmpty()) {
  	  	String targeted = req.getTargetedNodes().iterator().next();
  	  	TestChannelCallback callback = getCallback(targeted);
  	  	if(callback == null) {
  	  		throw new IllegalArgumentException("No node for: " + targeted);
  	  	}
  	  	callback.getController().onRequest(getNode(), req);
  	  }
		}
	}
	
	@Override
	public Set<SynchronousControlResponse> sendSynchronousRequest(
	    Set<String> targetedNodes, SynchronousControlRequest request)
	    throws InterruptedException, IOException {

		Set<SynchronousControlResponse> responses = new HashSet<SynchronousControlResponse>();
		if(!down) {
			for(String targeted : targetedNodes) {
				TestChannelCallback callback = getCallback(targeted);
				if(callback != null && !callback.down) {
					responses.add(callback.getController().onSynchronousRequest(getNode(), request));
				}
			}
		}
		
		return responses;
	}
	
	@Override
	public void sendResponse(String masterNode, ControlResponse res) {
		if(!down) {
    	TestChannelCallback callback = getCallback(masterNode);
    	callback.getController().onResponse(getNode(), res);
		}
	}
	
	public EventChannelController getController() {
	  return controller;
  }
	
	@Override
	public void forceResyncOf(Set<String> targetedNodes) {
	  forceResync = targetedNodes;
	}
	
	private TestChannelCallback getCallback(String node) {
		NodeRegistration reg = siblings.get(node);
  	if(reg == null) {
  		throw new IllegalArgumentException("No node for: " + node);
  	}
  	return reg.node;
	}

	public class TestCallbackAddress implements ServerAddress {
		
		private String uuid = UUID.randomUUID().toString();; 
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof TestCallbackAddress) {
				TestCallbackAddress addr = (TestCallbackAddress) obj;
				return addr.uuid.equals(uuid);
			} 
			return false;
		}
		
		@Override
		public String getTransportType() {
			return getClass().getName();
		}
	}
	
}
