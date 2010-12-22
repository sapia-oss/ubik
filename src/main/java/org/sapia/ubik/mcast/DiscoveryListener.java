package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;


/**
 * Listens for the discovery of new nodes in a given event channel.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface DiscoveryListener {
  /**
   * This method is called by the {@link EventChannel} to which
   * this instance belongs.
   *
   * @param addr a {@link ServerAddress} that corresponds to the address physical
   * address of the discovered node.
   *
   * @param evt the {@link RemoteEvent} that is received from the
   * discovered event channel node.
   */
  public void onDiscovery(ServerAddress addr, RemoteEvent evt);
}
