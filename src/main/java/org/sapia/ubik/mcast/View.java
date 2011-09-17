package org.sapia.ubik.mcast;

import org.sapia.ubik.mcast.EventChannelStateListener.EventChannelEvent;
import org.sapia.ubik.net.ServerAddress;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Encapsulates the addresses of the nodes that compose an event channel. An
 * instance of this class is encapsulated by an <code>EventChannel</code>. It
 * provides a "view" of the domain.
 * <p>
 * An instance of this class encapsulates the address of each of the sibling of
 * an {@link EventChannel} node.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class View {
  private Map<NodeInfo, Long>    _addresses  = new ConcurrentHashMap<NodeInfo, Long>();
  private Map<String, NodeInfo>  _nodeToAddr = new ConcurrentHashMap<String, NodeInfo>();
  private long                   _timeout;
  private List<SoftReference<EventChannelStateListener>> _listeners = Collections.synchronizedList(new ArrayList<SoftReference<EventChannelStateListener>>());

  /**
   * Constructor for View.
   */
  public View(long timeout) {
    _timeout = timeout;
  }
  
  /**
   * Adds the given listener to this instance, which will be kept in a {@link SoftReference}.
   * 
   * @param listener an {@link EventChannelStateListener}.
   */
  public void addEventChannelStateListener(EventChannelStateListener listener){
    _listeners.add(new SoftReference<EventChannelStateListener>(listener));
  }

  /**
   * Returns this instance's <code>List</code> of <code>ServerAddress</code>es.
   *
   * @return a <code>List</code> of <code>ServerAddress</code>es.
   */
  public List<ServerAddress> getHosts() {
    synchronized(_addresses){
      List<ServerAddress> toReturn = new ArrayList<ServerAddress>(_addresses.size());
      for(NodeInfo info : _addresses.keySet()){
        toReturn.add(info.addr);
      }
      return toReturn;
    }
  }

  /**
   * Returns the <code>ServerAddress</code> corresponding to the given
   * node.
   *
   * @return a <code>ServerAddress</code>.
   */
  public ServerAddress getAddressFor(String node) {
    NodeInfo info = (NodeInfo) _nodeToAddr.get(node);

    return info.addr;
  }
  
  /**
   * @param timeout the timeout after which nodes that haven't sent a heartbeat
   * are removed from this instance.
   */
  public void setTimeout(long timeout){
    _timeout = timeout;
  }
  
  /**
   * @return this instance's heartbeat timeout.
   */
  public long getTimeout() {
    return _timeout;
  }
  
  /**
   * Adds the given address to this instance.
   *
   * @param addr the <code>ServerAddress</code> corresponding to a remote
   * <code>EventChannel</code>.
   * @param node node identifier.
   */
  boolean addHost(ServerAddress addr, String node) {
    NodeInfo info = new NodeInfo(addr, node);
    synchronized(_addresses){
      if(_addresses.put(info, new Long(System.currentTimeMillis())) == null){
        _nodeToAddr.put(node, info);
        return true;
      }    
    }
    return false;
  }

  /**
   * Updates the "last access" flag corresponding to the passed in
   * <code>ServerAddress</code>.
   *
   * @param <code>ServerAddress</code>.
   * @param node node identifier.
   */
  void heartbeat(ServerAddress addr, String node) {
    synchronized(_addresses){
      NodeInfo info = new NodeInfo(addr, node);
      if(_addresses.put(info, new Long(System.currentTimeMillis())) == null){
        _nodeToAddr.put(node, info);
        notifyListeners(new EventChannelEvent(node, addr), true);
      }
    }
  }

  /**
   * Removes the "dead" (timed-out) hosts from this instance.
   */
  void removeDeadHosts() {
    
    synchronized(_addresses){
    
      List<NodeInfo> deadNodes = new ArrayList<NodeInfo>(_addresses.size() / 2);
    
      for(Map.Entry<NodeInfo, Long> entry:_addresses.entrySet()){
          if ((System.currentTimeMillis() -
                entry.getValue().longValue()) > _timeout) {
            deadNodes.add(entry.getKey());
          }
      }
    
      for(NodeInfo dead: deadNodes){
        _addresses.remove(dead);
        _nodeToAddr.remove(dead.node);
        notifyListeners(new EventChannelEvent(dead.node, dead.addr), false);
      }
    }
  }
  
  private void notifyListeners(EventChannelEvent event, boolean added){
    synchronized(_listeners){
      for(int i = 0; i < _listeners.size(); i++){
        SoftReference<EventChannelStateListener> listenerRef = _listeners.get(i);
        EventChannelStateListener listener = listenerRef.get();
        if(listener == null){
          _listeners.remove(i);
        }
        else{
          if(added){
            listener.onUp(event);
          }
          else{
            listener.onDown(event);
          }
        }
      }
    }
  }

  /*//////////////////////////////////////////////////
                      INNER CLASSES
  //////////////////////////////////////////////////*/
  static class NodeInfo {
    final ServerAddress addr;
    final String        node;

    NodeInfo(ServerAddress addr, String node) {
      this.addr   = addr;
      this.node   = node;
    }

    public boolean equals(Object obj) {
      NodeInfo inf = (NodeInfo) obj;

      return inf.addr.equals(addr) && inf.node.equals(node);
    }

    public int hashCode() {
      return addr.hashCode() * 31 ^ node.hashCode() * 31;
    }
  }
  
}
