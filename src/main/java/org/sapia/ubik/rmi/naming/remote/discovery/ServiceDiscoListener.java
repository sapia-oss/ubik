package org.sapia.ubik.rmi.naming.remote.discovery;


/**
 * An instance of this class is used to listen for new services that appear in the
 * distributed JNDI.
 *
 * @see org.sapia.ubik.rmi.naming.remote.discovery.ServiceDiscoveryEvent
 * @see org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext#addServiceDiscoListener(ServiceDiscoListener)
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ServiceDiscoListener {
  /**
   * Called when a new service is bound to the distributed JNDI tree.
   *
   * @param evt a <code>ServiceDiscoveryEvent</code>.
   */
  public void onServiceDiscovered(ServiceDiscoveryEvent evt);
}
