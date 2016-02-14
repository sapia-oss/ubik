package org.sapia.ubik.mcast.control;

import org.sapia.ubik.net.ServerAddress;

/**
 * Specifies the behavior for handling gossip notifications.
 * 
 * @author yduchesne
 *
 */
public interface GossipNotificationHandler {

  /**
   * @param originNode
   *          the identifier of the node from which the notification originates.
   * @param originAddress
   *          the {@link ServerAddress} of the origin node.
   * @param notif
   *          the {@link GossipNotification} to handle.
   */
  public void handle(String originNode, ServerAddress originAddress, GossipNotification notif);
}
