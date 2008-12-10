package org.sapia.ubik.rmi.naming.remote.discovery;


/**
 * An instance of this interface is notified when a new JNDI server appears on the network.
 *
 * @see org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext#addJndiDiscoListener(JndiDiscoListener)
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface JndiDiscoListener {
  public void onJndiDiscovered(javax.naming.Context ctx);
}
