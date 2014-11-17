package org.sapia.ubik.mcast;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.SoftReferenceList;

/**
 * Encapsulates the addresses of the nodes that compose an event channel. An
 * instance of this class is encapsulated by an {@link EventChannel}. It
 * provides a "view" of the domain.
 * <p>
 * An instance of this class encapsulates the address of each of the peers of an
 * {@link EventChannel} node.
 * 
 * @author Yanick Duchesne
 */
public class View {

  private Category log = Log.createCategory(getClass());
  private Map<String, NodeInfo> nodeToNodeInfo = new ConcurrentHashMap<String, NodeInfo>();
  private SoftReferenceList<EventChannelStateListener> listeners = new SoftReferenceList<EventChannelStateListener>();

  /**
   * Adds the given listener to this instance, which will be kept in a
   * {@link SoftReference}.
   * 
   * @param listener
   *          an {@link EventChannelStateListener}.
   */
  public void addEventChannelStateListener(EventChannelStateListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes the given listener from this instance.
   * 
   * @param listener
   */
  public boolean removeEventChannelStateListener(EventChannelStateListener listener) {
    return listeners.remove(listener);
  }

  /**
   * Returns this instance's {@link List} of {@link ServerAddress}es.
   * 
   * @return a {@link List} of {@link ServerAddress}es.
   */
  public List<ServerAddress> getNodeAddresses() {
    return Collects.convertAsList(nodeToNodeInfo.values(), new Func<ServerAddress, NodeInfo>() {
      public ServerAddress call(NodeInfo arg) {
        return arg.addr;
      }
    });
  }

  /**
   * Returns this instance's {@link List} of nodes.
   * 
   * @return a {@link List} of nodes.
   */
  public List<String> getNodes() {
    return Collects.convertAsList(nodeToNodeInfo.values(), new Func<String, NodeInfo>() {
      public String call(NodeInfo arg) {
        return arg.node;
      }
    });
  }
  
  /**
   * @return the number of nodes held by this instance.
   */
  public int getNodeCount() {
    return nodeToNodeInfo.size();
  }

  /**
   * Returns this instance's {@link Set} of nodes.
   * 
   * @return a {@link Set} of nodes.
   */
  public Set<String> getNodesAsSet() {
    return Collects.convertAsSet(nodeToNodeInfo.values(), new Func<String, NodeInfo>() {
      public String call(NodeInfo arg) {
        return arg.node;
      }
    });
  }
  
  /**
   * @return this instance's {@link List} of {@link NodeInfo} instances.
   */
  public List<NodeInfo> getNodeInfos() {
    return new ArrayList<>(nodeToNodeInfo.values());
  }

  /**
   * Returns the {@link ServerAddress} corresponding to the given node.
   * 
   * @return a {@link ServerAddress}.
   */
  public ServerAddress getAddressFor(String node) {
    NodeInfo info = (NodeInfo) nodeToNodeInfo.get(node);
    if (info == null)
      return null;
    return info.addr;
  }

  /**
   * Adds the given address to this instance.
   * 
   * @param addr
   *          the {@link ServerAddress} corresponding to a remote
   *          {@link EventChannel}.
   * @param node
   *          node identifier.
   */
  boolean addHost(ServerAddress addr, String node) {
    NodeInfo info = new NodeInfo(addr, node);
    if (nodeToNodeInfo.put(node, info) == null) {
      log.debug("Adding node %s at address %s to view", node, addr);
      notifyListeners(new EventChannelEvent(node, addr), true);
      return true;
    }
    return false;
  }

  /**
   * Invoked when a heartbeat request is received.
   * <p>
   * Updates the "last access" flag corresponding to the passed in
   * {@link ServerAddress}.
   * 
   * @param addr
   *          a {@link ServerAddress}.
   * @param node
   *          a node identifier.
   */
  void heartbeatResponse(ServerAddress addr, String node) {
    NodeInfo info = new NodeInfo(addr, node);
    log.debug("Received heartbeat response from %s", node);
    if (nodeToNodeInfo.put(node, info) == null) {
      log.debug("Adding node %s at address %s to view", node, addr);
      notifyListeners(new EventChannelEvent(node, addr), true);
    } else {
      EventChannelEvent event = new EventChannelEvent(node, addr);
      for (EventChannelStateListener listener : listeners) {
        listener.onHeartbeatResponse(event);
      }
    }
  }
  
  /**
   * Invoked when a heartbeat request is received.
   * <p>
   * Updates the "last access" flag corresponding to the passed in
   * {@link ServerAddress}.
   * 
   * @param addr
   *          a {@link ServerAddress}.
   * @param node
   *          a node identifier.
   */
  void heartbeatRequest(ServerAddress addr, String node) {
    NodeInfo info = new NodeInfo(addr, node);
    log.debug("Received heartbeat response from %s", node);
    if (nodeToNodeInfo.put(node, info) == null) {
      log.debug("Adding node %s at address %s to view", node, addr);
      notifyListeners(new EventChannelEvent(node, addr), true);
    } else {
      EventChannelEvent event = new EventChannelEvent(node, addr);
      for (EventChannelStateListener listener : listeners) {
        listener.onHeartbeatRequest(event);
      }
    }
  }

  /**
   * @param node
   *          a node identifier. 
   */
  void removeDeadNode(String node) {
    NodeInfo removed = nodeToNodeInfo.remove(node);
    if (removed != null) {
      log.debug("Removing dead node %s", node);
      notifyListeners(new EventChannelEvent(removed.node, removed.addr), false);
    }
  }
  
  /**
   * @param nodes the {@link List} of {@link NodeInfo} instances corresponding to the nodes
   * to remove.
   */
  void update(List<NodeInfo> nodes) {
    Set<String> actual = Collects.convertAsSet(nodes, new Func<String, NodeInfo>() {
      @Override
      public String call(NodeInfo arg) {
        return arg.node;
      }
    });
    Set<String> current = getNodesAsSet();
    Set<String> toRemove = new HashSet<>();
    for (String c : current) {
      if (!actual.contains(c)) {
        toRemove.add(c);
      }
    }
    
    for (NodeInfo n : nodes) {
      if (!nodeToNodeInfo.containsKey(n.node)) {
        addHost(n.addr, n.node);
      }
    }
    
    for (String r : toRemove) {
      removeDeadNode(r);
    }
  }

  /**
   * @param node a node identifier.
   * @return <code>true</code> if this instance has the corresponding node.
   */
  boolean containsNode(String node) {
    return nodeToNodeInfo.containsKey(node);
  }

  private void notifyListeners(EventChannelEvent event, boolean added) {
    synchronized (listeners) {
      for (EventChannelStateListener listener : listeners) {
        if (added) {
          log.debug("Node %s is up", event.getNode());
          listener.onUp(event);
        } else {
          log.debug("Node %s is down", event.getNode());
          listener.onDown(event);
        }
      }
    }
  }

}
