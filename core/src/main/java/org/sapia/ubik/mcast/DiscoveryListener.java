package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;

/**
 * Listens for the discovery of new nodes in a given event channel.
 * 
 * @author Yanick Duchesne
 */
public interface DiscoveryListener {

  /**
   * This method is called by the {@link EventChannel} to which this instance
   * belongs.
   * 
   * @param addr
   *          a {@link ServerAddress} that corresponds to the address physical
   *          address of the discovered node.
   * 
   * @param evt
   *          the {@link RemoteEvent} that is received from the discovered event
   *          channel node.
   */
  public void onDiscovery(ServerAddress addr, RemoteEvent evt);
}
