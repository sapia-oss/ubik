package org.sapia.ubik.rmi.naming.remote.discovery;

/**
 * An instance of this class is used to listen for new services that appear in
 * the distributed JNDI.
 * 
 * @see org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent
 * @see org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext#addServiceDiscoListener(ServiceDiscoListener)
 * 
 * @author Yanick Duchesne
 */
public interface ServiceDiscoListener {
  /**
   * Called when a new service is bound to the distributed JNDI tree.
   * 
   * @param evt
   *          a {@link ServiceDiscoveryEvent}.
   */
  public void onServiceDiscovered(ServiceDiscoveryEvent evt);
}
