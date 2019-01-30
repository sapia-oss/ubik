package org.sapia.ubik.mcast;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.Pause;
import org.sapia.ubik.util.SoftReferenceList;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.RealtimeClock;

/**
 * Encapsulates the addresses of the nodes that compose an event channel. An
 * instance of this class is encapsulated by an {@link EventChannel}. It
 * provides a "view" of the domain.
 * <p>
 * An instance of this class encapsulates the address of each of the peers of an
 * {@link EventChannel} node.
 * 
 * @author yduchesne
 */
public class View {

  private enum ViewEventType {
    ADDED, REMOVED, LEFT
  }
  
  private Category log = Log.createCategory(getClass());

  private SysClock clock;
  private String node;
  private Map<String, NodeInfo>                        nodeToNodeInfo = new ConcurrentHashMap<String, NodeInfo>();
  private Map<String, NodeInfo>                        deadNodes      = new ConcurrentHashMap<String, NodeInfo>();
  private SoftReferenceList<EventChannelStateListener> listeners      = new SoftReferenceList<EventChannelStateListener>();
  private Object                                       peerWaitLock   = new Object();
  private Object                                       mutationLock   = new Object();
  
  /**
   * @param node the node identifier corresponding to the cluster member node to which this instance is associated.
   */
  public View(String node) {
    this(RealtimeClock.getInstance(), node);
  }

  /**
   * @param clock a {@link SysClock} instance, to internally use for assigning timestamps to {@link NodeInfo} instances.
   * @param node the node identifier corresponding to the cluster member node to which this instance is associated.
   */
  public View(SysClock clock, String node) {
    this.clock = clock;
    this.node = node;
  }
  
  /**
   * @param timeout the maximum amount of time to wait for.
   * @param unit    the {@link TimeUnit} in which the given timeout is expressed.
   * @throws InterruptedException if the calling thread is interrupted while waiting.
   */
  public void tryAwaitPeers(long timeout, TimeUnit unit) throws InterruptedException {
    Pause pause = new Pause(unit.toMillis(timeout));
    while (nodeToNodeInfo.isEmpty() && !pause.isOver()) {
      synchronized (peerWaitLock) {
        peerWaitLock.wait(pause.remainingNotZero());
      }
    }
	}
  
  /**
   * @param timeout the maximum amount of time to wait for.
   * @param unit    the {@link TimeUnit} in which the given timeout is expressed.
   * @param minimum the minimum number of peers to wait for.
   * @throws InterruptedException if the calling thread is interrupted while waiting.
   */
  public void tryAwaitPeers(long timeout, TimeUnit unit, int minimum) throws InterruptedException {
    Pause pause = new Pause(unit.toMillis(timeout));
    while (nodeToNodeInfo.size() < minimum && !pause.isOver()) {
      synchronized (peerWaitLock) {
        peerWaitLock.wait(pause.remainingNotZero());
      }
    }
  }

  /**
   * @param timeout the maximum amount of time to wait for.
   * @param unit    the {@link TimeUnit} in which the given timeout is expressed.
   * @throws InterruptedException  if the calling thread is interrupted while waiting.
   * @throws IllegalStateException if no peers where discovered within the specified timeout.
   */
  public void awaitPeers(long timeout, TimeUnit unit) throws InterruptedException, IllegalStateException {
    tryAwaitPeers(timeout, unit);
    Assertions.illegalState(nodeToNodeInfo.isEmpty(), "No peers discovered withing the specified timeout");
  }
  
  /**
   * @param timeout the maximum amount of time to wait for.
   * @param unit    the {@link TimeUnit} in which the given timeout is expressed.
   * @param minimum the minimum number of peers to wait for.
   * @throws InterruptedException  if the calling thread is interrupted while waiting.
   * @throws IllegalStateException if the minimum number of peers where was not discovered 
   *                               within the specified timeout.
   */
  public void awaitPeers(long timeout, TimeUnit unit, int minimum) throws InterruptedException, IllegalStateException {
    tryAwaitPeers(timeout, unit, minimum);
    Assertions.illegalState(nodeToNodeInfo.isEmpty(), "No peers discovered withing the specified timeout");
  }
  
  
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
        return arg.getAddr();
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
        return arg.getNode();
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
   * Returns a copy of this instance's {@link Set} of nodes.
   * 
   * @return a {@link Set} of nodes.
   */
  public Set<String> getNodesAsSet() {
    return Collects.convertAsSet(nodeToNodeInfo.values(), new Func<String, NodeInfo>() {
      public String call(NodeInfo arg) {
        return arg.getNode();
      }
    });
  }
  
  /**
   * @return a copy of this instance's {@link List} of {@link NodeInfo} instances.
   */
  public List<NodeInfo> getNodeInfos() {
    return new ArrayList<>(nodeToNodeInfo.values());
  }
  
  /**
   * @param filter a {@link Condition} to use as filter.
   * @return a {@link List} of {@link NodeInfo} instances corresponding to the given condition.
   */
  public List<NodeInfo> getNodeInfos(Condition<NodeInfo> filter) {
    return Collects.filterAsList(nodeToNodeInfo.values(), filter);
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
    return info.getAddr();
  }
 
  /**
   * @param node the identifier of the expected {@link NodeInfo}.
   * @return the {@link NodeInfo} matching the given node ID, or <code>null</code>
   * if no such instance was found.
   */
  public NodeInfo getNodeInfo(String node) {
    return nodeToNodeInfo.get(node);
  }
  
  /**
   * @param node a node identifier.
   * @return <code>true</code> if this instance has the corresponding node.
   */
  boolean containsNode(String node) {
    return nodeToNodeInfo.containsKey(node);
  }
  
  boolean isNodeDead(String node) {
    return deadNodes.containsKey(node);
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
    Assertions.illegalState(node.equals(this.node), "Cannot add self as member node: %s", node);
    boolean isNodeAdded = false;
    synchronized (mutationLock) {
      NodeInfo info = nodeToNodeInfo.get(node);
      if (info == null) {
        if (deadNodes.containsKey(node)) {
          log.warning("Not adding node %s at address %s because it exist in dead node list", node, addr);
        } else {
          info = new NodeInfo(addr, node);
          info.touch(clock);
          nodeToNodeInfo.put(node, info);
          isNodeAdded = true;
          log.info("Added node %s at address %s to view", node, addr);
        }
      }
    }
    // Notify listeners outside of the muation lock
    if (isNodeAdded) {
      notifyListeners(new EventChannelEvent(node, addr), ViewEventType.ADDED);
    }
    
    return isNodeAdded;
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
   * @param clock
   *          the {@link SysClock} instance to use to obtain a current timestamp value.
   */
  void heartbeat(ServerAddress addr, String node, SysClock clock) {
    Assertions.illegalState(node.equals(this.node), "Cannot add self as member node: %s", node);
    boolean isNewNode = false;
    synchronized (mutationLock) {
      NodeInfo info = nodeToNodeInfo.get(node);
      if (info == null) {
        if (deadNodes.containsKey(node)) {
          log.warning("Got hearthbeat from dead node %s at address %s, removing in from dead node list", node, addr);
          deadNodes.remove(node);
        }
        info = new NodeInfo(addr, node);
        info.touch(clock);
        nodeToNodeInfo.put(node, info);
        isNewNode = true;
        log.info("Added node %s (on heartbeat) at address %s to view", node, addr);
      } else {
        info.touch(clock);
      }
    }
    // Notify listeners outside of the muation lock
    if (isNewNode) {
      notifyListeners(new EventChannelEvent(node, addr), ViewEventType.ADDED);
    }
  }

  /**
   * @param node
   *          a node identifier. 
   */
  void removeDeadNode(String node) {
    NodeInfo toRemoved = null;
    synchronized (mutationLock) {
      toRemoved = nodeToNodeInfo.get(node);
      if (toRemoved != null) {
        deadNodes.put(node, toRemoved);
        nodeToNodeInfo.remove(node);
        toRemoved.down(clock);      
        log.info("Removed dead node %s", node);
      }
    }
    // Notify listeners outside of the muation lock
    if (toRemoved != null) {
      notifyListeners(new EventChannelEvent(toRemoved.getNode(), toRemoved.getAddr()), ViewEventType.REMOVED);
    }
  }
  
  void cleanupDeadNodeList(long gracePeriodMillis) {
    synchronized (mutationLock) {
      long now = clock.currentTimeMillis();
      List<String> nodesToDelete = deadNodes.entrySet().stream().
          filter(e -> (e.getValue().getTimestamp() + gracePeriodMillis) < now).
          map(e -> e.getKey()).
          collect(Collectors.toList());
      
      nodesToDelete.forEach(n -> {
        log.info("removing expired node %s from dead node list", n);
        deadNodes.remove(n);
      });
    }
  }

  /**
   * @param node
   *          a node identifier. 
   */
  void removeLeavingNode(String node) {
    NodeInfo removed = null;
    synchronized (mutationLock) {
      removed = nodeToNodeInfo.remove(node);
    }
    // Notify listeners outside of the muation lock
    if (removed != null) {
      log.debug("Removing leaving node %s", node);
      notifyListeners(new EventChannelEvent(removed.getNode(), removed.getAddr()), ViewEventType.LEFT);
    }
  }

  /**
   * Invoked when it is actually the node corresponding to this view
   * that leaves the domain.
   */
  void clearView() {
    log.info("Clearing view, removing all nodes");
    for (NodeInfo removed : nodeToNodeInfo.values()) {
      log.debug("Removing node %s from view", removed.getNode());
      notifyListeners(new EventChannelEvent(removed.getNode(), removed.getAddr()), ViewEventType.LEFT);
    }
  }

  private void notifyListeners(EventChannelEvent event, ViewEventType eventType) {
    synchronized (peerWaitLock) {
      peerWaitLock.notifyAll();
    }
    synchronized (listeners) {
      for (EventChannelStateListener listener : listeners) {
        try {
          if (eventType == ViewEventType.ADDED) {
            log.info("Node %s is up, notify listeners", event.getNode());
            listener.onUp(event);
          } else if (eventType == ViewEventType.REMOVED) {
            log.info("Node %s is down, notify listeners", event.getNode());
            listener.onDown(event);
          } else if (eventType == ViewEventType.LEFT) {
            log.info("Node %s left cluster, notify listeners", event.getNode());
            listener.onLeft(event);
          } else {
            log.error("Unknown view event type: %s", eventType);
          }
        } catch (Exception e) {
          log.warning("System error notifying listeners of view event " + eventType, e);
        }
      }
    }
  }

}
