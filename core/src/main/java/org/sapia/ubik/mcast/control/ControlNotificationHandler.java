package org.sapia.ubik.mcast.control;

import org.sapia.ubik.net.ServerAddress;

/**
 * A handler of {@link ControlNotificationHandler}.
 * 
 * @author yduchesne
 * 
 */
public interface ControlNotificationHandler {

  /**
   * @param originNode
   *          the identifier of the node from which the notification originates.
   * @param originAddress
   *          the {@link ServerAddress} of the node from which the notification originates.
   * @param notif
   *          the {@link ControlNotification} to handle.
   */
  public void handle(String originNode, ServerAddress originAddress, ControlNotification notif);

}
