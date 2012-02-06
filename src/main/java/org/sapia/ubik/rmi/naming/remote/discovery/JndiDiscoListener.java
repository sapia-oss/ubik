package org.sapia.ubik.rmi.naming.remote.discovery;


/**
 * An instance of this interface is notified when a new JNDI server appears on the network.
 *
 * @see org.sapia.ubik.rmi.naming.remote.proxy.ReliableLocalContext#addJndiDiscoListener(JndiDiscoListener)
 *
 * @author Yanick Duchesne
 */
public interface JndiDiscoListener {
  public void onJndiDiscovered(javax.naming.Context ctx);
}
