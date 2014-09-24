package org.sapia.ubik.mcast.control;

import org.sapia.ubik.net.ServerAddress;

/**
 * Specifies a handler of {@link ControlEvent}s.
 * 
 * @author yduchesne
 *
 */
public interface ControlEventHandler {

  /**
   * Handles the given event.
   * 
   * @param originNode
   *          the node from which the event originates.
   * @param originAddress
   *          the {@link ServerAddress} of the node from which the event originates.
   * @param event
   *          a {@link ControlEvent}.
   */
  public void handle(String originNode, ServerAddress originAddress, ControlEvent event);
}
