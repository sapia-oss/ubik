package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;

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

  /**
   * Constructor for View.
   */
  public View(long timeout) {
    _timeout = timeout;
  }

  /**
   * Returns this instance's <code>List</code> of <code>ServerAddress</code>es.
   *
   * @return a <code>List</code> of <code>ServerAddress</code>es.
   */
  public synchronized List<ServerAddress> getHosts() {
    List<ServerAddress> toReturn = new ArrayList<ServerAddress>(_addresses.size());
    for(NodeInfo info : _addresses.keySet()){
      toReturn.add(info.addr);
    }
    return toReturn;
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
   * Adds the given address to this instance.
   *
   * @param addr the <code>ServerAddress</code> corresponding to a remote
   * <code>EventChannel</code>.
   * @param node node identifier.
   */
  void addHost(ServerAddress addr, String node) {
    NodeInfo info = new NodeInfo(addr, node);
    _nodeToAddr.put(node, info);
    _addresses.put(info, new Long(System.currentTimeMillis()));
  }

  /**
   * Updates the "last access" flag corresponding to the passed in
   * <code>ServerAddress</code>.
   *
   * @param <code>ServerAddress</code>.
   * @param node node identifier.
   */
  void heartbeat(ServerAddress addr, String node) {
    _addresses.put(new NodeInfo(addr, node),
      new Long(System.currentTimeMillis()));
  }

  /**
   * Removes the "dead" (timed-out) hosts from this instance.
   */
  void removeDeadHosts() {
    
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
      return addr.hashCode();
    }
  }
}
