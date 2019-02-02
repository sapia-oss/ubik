package org.sapia.ubik.mcast.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.TimeValue;
import org.sapia.ubik.util.UbikMetrics;

public class TestChannelCallback implements EventChannelFacade {

  class NodeRegistration {
    long lastHeartbeatTime;
    TestChannelCallback node;

    NodeRegistration(TestChannelCallback node) {
      this.node = node;
    }
  }

  private String node;
  private TestCallbackAddress address = new TestCallbackAddress();
  private EventChannelController controller;
  private Map<String, NodeRegistration> siblings = new ConcurrentHashMap<String, NodeRegistration>();
  private Map<String, NodeRegistration> deadSiblings = new ConcurrentHashMap<String, NodeRegistration>();
  private volatile boolean down;
  private volatile Set<String> forceResync;

  public TestChannelCallback(String node, SysClock clock, ControllerConfiguration config, UbikMetrics metrics) {
    this.node = node;
    this.controller = new EventChannelController(clock, config, this, metrics);
  }

  public TestChannelCallback addSibling(TestChannelCallback sibling) {
    if (!this.node.equals(sibling.getNode())) {
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
  public boolean addNewNode(String node, ServerAddress addr) {
    return false;
  }
  
  @Override
  public boolean containsNode(String node) {
    return siblings.containsKey(node);
  }
  
  @Override
  public NodeInfo getNodeInfoFor(String node) {
    NodeRegistration reg = siblings.get(node);
    if (reg != null) {
      return new NodeInfo(reg.node.getAddress(), reg.node.getNode());
    }
    return null;
  }
  
  
  @Override
  public int getNodeCount() {
    return siblings.size();
  }
  
  @Override
  public List<NodeInfo> getView() {
    return new ArrayList<>();
  }
  
  @Override
  public List<NodeInfo> getView(Condition<NodeInfo> filter) {
    return getView();
  }
  
  @Override
  public Future<Void> sendBroadcastEvent(ControlEvent event) {
    return CompletableFuture.completedFuture(null);
  }
  
  @Override
  public Future<Void> sendUnicastEvent(ServerAddress destination, ControlEvent event) {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void heartbeat(String node, ServerAddress unicastAddress) {
  }
  
  @Override
  public void sendNotification(ControlNotification notif) {
    if (!down) {
      notif.getTargetedNodes().remove(node);
      if (!notif.getTargetedNodes().isEmpty()) {
        String targeted = notif.getTargetedNodes().iterator().next();
        notif.getTargetedNodes().remove(targeted);
        TestChannelCallback callback = getCallback(targeted);
        if (callback == null) {
          throw new IllegalArgumentException("No node for: " + targeted);
        }
        callback.getController().onNotification(getNode(), getAddress(), notif);
      }
    }
  }
  
  @Override
  public void sendGossipNotification(GossipNotification notif) {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public Set<SynchronousControlResponse> sendSynchronousRequest(Set<String> targetedNodes, SynchronousControlRequest request, TimeValue timeout)
      throws InterruptedException, IOException {
    Set<SynchronousControlResponse> responses = new HashSet<SynchronousControlResponse>();
    if (!down) {
      for (String targeted : targetedNodes) {
        TestChannelCallback callback = getCallback(targeted);
        if (callback != null && !callback.down) {
          responses.add(callback.getController().onSynchronousRequest(getNode(), getAddress(), request));
        }
      }
    }
    return responses;
  }
  
  @Override
  public Set<SynchronousControlResponse> sendSynchronousRequests(String[] targetedNodes, SynchronousControlRequest[] requests, TimeValue timeout)
      throws InterruptedException, IOException {
    Set<SynchronousControlResponse> responses = new HashSet<SynchronousControlResponse>();
    if (!down) {
      for (int i = 0; i < targetedNodes.length; i++) {
        TestChannelCallback callback = getCallback(targetedNodes[i]);
        if (callback != null && !callback.down) {
          responses.add(callback.getController().onSynchronousRequest(getNode(), getAddress(), requests[i]));
        }
      }
    }
    return responses;
  }

  public EventChannelController getController() {
    return controller;
  }

  private TestChannelCallback getCallback(String node) {
    NodeRegistration reg = siblings.get(node);
    if (reg == null) {
      throw new IllegalArgumentException("No node for: " + node);
    }
    return reg.node;
  }

  public class TestCallbackAddress implements ServerAddress {

    private String uuid = UUID.randomUUID().toString();;

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof TestCallbackAddress) {
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

  @Override
  public void cleanDeadNodes(long gracePeriodMillis) {
  }

}
