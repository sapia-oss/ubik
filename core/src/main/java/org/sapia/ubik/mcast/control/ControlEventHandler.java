package org.sapia.ubik.mcast.control;

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
   *          the node from which the request originates.
   * @param event
   *          a {@link ControlEvent}.
   */
  public void handle(String originNode, ControlEvent event);
}
