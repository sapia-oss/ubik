package org.sapia.ubik.mcast;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent;
import org.sapia.ubik.net.ServerAddress;


/**
 * Encapsulates the addresses of the nodes that compose an event channel. An
 * instance of this class is encapsulated by an {@link EventChannel}. It
 * provides a "view" of the domain.
 * <p>
 * An instance of this class encapsulates the address of each of the sibling of
 * an {@link EventChannel} node.
 *
 * @author Yanick Duchesne
 */
public class View {
  
  private Category               log                   = Log.createCategory(getClass());
  private Map<String, NodeInfo>  nodeToNodeInfo        = new ConcurrentHashMap<String, NodeInfo>();
  private volatile long          timeout;
  private List<SoftReference<EventChannelStateListener>> listeners = Collections.synchronizedList(new ArrayList<SoftReference<EventChannelStateListener>>());

  /**
   * Constructor for View.
   */
  public View(long timeout) {
    this.timeout = timeout;
  }
  
  /**
   * Adds the given listener to this instance, which will be kept in a {@link SoftReference}.
   * 
   * @param listener an {@link EventChannelStateListener}.
   */
  public void addEventChannelStateListener(EventChannelStateListener listener){
    listeners.add(new SoftReference<EventChannelStateListener>(listener));
  }
  
  /**
   * Removes the given listener from this instance.
   * 
   * @param listener
   */
  public boolean removeEventChannelStateListener(EventChannelStateListener listener) {
    synchronized(listeners) {
      for(int i = 0; i < listeners.size(); i++) {
        SoftReference<EventChannelStateListener> listenerRef = listeners.get(i);
        EventChannelStateListener registered = listenerRef.get();
        if(registered != null && registered.equals(listener)) {
          listeners.remove(i);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns this instance's {@link List} of {@link ServerAddress}es.
   *
   * @return a {@link List} of {@link ServerAddress}es.
   */
  public List<ServerAddress> getHosts() {
    List<ServerAddress> toReturn = new ArrayList<ServerAddress>(nodeToNodeInfo.size());
    for(NodeInfo info : nodeToNodeInfo.values()){
      toReturn.add(info.addr);
    }
    return toReturn;
  }

  /**
   * Returns the {@link ServerAddress} corresponding to the given
   * node.
   *
   * @return a {@link ServerAddress}.
   */
  public ServerAddress getAddressFor(String node) {
    NodeInfo info = (NodeInfo) nodeToNodeInfo.get(node);

    return info.addr;
  }
  
  /**
   * @param timeout the timeout after which nodes that haven't sent a heartbeat
   * are removed from this instance.
   */
  public void setTimeout(long timeout){
    this.timeout = timeout;
  }
  
  /**
   * @return this instance's heartbeat timeout.
   */
  public long getTimeout() {
    return timeout;
  }
  
  /**
   * Adds the given address to this instance.
   *
   * @param addr the {@link ServerAddress} corresponding to a remote {@link EventChannel}.
   * @param node node identifier.
   */
  boolean addHost(ServerAddress addr, String node) {
    NodeInfo info = new NodeInfo(addr, node);
    if(nodeToNodeInfo.put(node, info) == null){
      log.debug("Adding node %s at address %s to view", node, addr);
      notifyListeners(new EventChannelEvent(node, addr), true);
      return true;
    }    
    return false;
  }

  /**
   * Updates the "last access" flag corresponding to the passed in {@link ServerAddress}.
   *
   * @param addr a {@link ServerAddress}.
   * @param node node identifier.
   */
  void heartbeat(ServerAddress addr, String node) {
    NodeInfo info = new NodeInfo(addr, node);
    log.debug("Received heartbeat from %s", node);
    if(nodeToNodeInfo.put(node, info) == null){
      log.debug("Adding node %s at address %s to view", node, addr);
      notifyListeners(new EventChannelEvent(node, addr), true);
    }
  }

  /**
   * Removes the "dead" (timed out) hosts from this instance.
   */
  void removeDeadHosts() {
    
    List<NodeInfo> deadNodes = new ArrayList<NodeInfo>(nodeToNodeInfo.size() / 2);
  
    for(NodeInfo node : nodeToNodeInfo.values()){
      if (node.isTimeoutReached(timeout)) {
        deadNodes.add(node);
      }
    }
  
    for(NodeInfo dead: deadNodes){
      log.debug("Removing dead host %s", dead.node);
      nodeToNodeInfo.remove(dead.node);
      notifyListeners(new EventChannelEvent(dead.node, dead.addr), false);
    }
  }
  
  private void notifyListeners(EventChannelEvent event, boolean added){
    synchronized(listeners){
      for(int i = 0; i < listeners.size(); i++){
        SoftReference<EventChannelStateListener> listenerRef = listeners.get(i);
        EventChannelStateListener listener = listenerRef.get();
        if(listener == null){
          listeners.remove(i--);
        }
        else{
          if(added){
            log.debug("Node %s is up", event.getNode());
            listener.onUp(event);
          }
          else{
            log.debug("Node %s is down", event.getNode());
            listener.onDown(event);
          }
        }
      }
    }
  }

  // --------------------------------------------------------------------------
  
  private static class NodeInfo {
    
    private ServerAddress addr;
    private String        node;
    private long          updateTime = System.currentTimeMillis();

    private NodeInfo(ServerAddress addr, String node) {
      this.addr   = addr;
      this.node   = node;
    }
    
    private boolean isTimeoutReached(long timeout) {
      return System.currentTimeMillis() - updateTime > timeout;
    }

    public boolean equals(Object obj) {
      if (obj instanceof NodeInfo) {
        NodeInfo inf = (NodeInfo) obj;
  
        return inf.addr.equals(addr) && inf.node.equals(node);
      }
      return false;
    }

    public int hashCode() {
      return addr.hashCode() * 31 + node.hashCode() * 31;
    }
  }
  
}
