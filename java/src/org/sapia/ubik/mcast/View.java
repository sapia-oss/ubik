package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;

import java.util.*;


/**
 * Encapsulates the addresses of the nodes that compose an event channel. An
 * instance of this class is encapsulated by an <code>EventChannel</code>. Its
 * provides a "view" of the domain.
 * <p>
 * An instance of this class encapsulates the address of each of the sibling of
 * an <code>EventChannel</code> node.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class View {
  private Map  _addresses  = new HashMap();
  private Map  _nodeToAddr = new HashMap();
  private long _timeout;

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
  public synchronized List getHosts() {
    return new AddressList(_addresses.keySet());
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
    Map.Entry   entry;
    Map.Entry[] hosts;

    synchronized (_addresses) {
      hosts = (Map.Entry[]) _addresses.entrySet().toArray(new Map.Entry[_addresses.size()]);

      NodeInfo info;

      for (int i = 0; i < hosts.length; i++) {
        if ((System.currentTimeMillis() -
              ((Long) hosts[i].getValue()).longValue()) > _timeout) {
          info = (NodeInfo) hosts[i].getKey();
          _addresses.remove(info);
          _nodeToAddr.remove(info.node);
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
      return addr.hashCode();
    }
  }

  public static class AddressList extends ArrayList {
    AddressList(Collection infos) {
      super(infos);
    }

    public Object get(int idx) {
      try {
        return ((NodeInfo) super.get(idx)).addr;
      } catch (ClassCastException e) {
        throw e;
      }
    }
  }
}
