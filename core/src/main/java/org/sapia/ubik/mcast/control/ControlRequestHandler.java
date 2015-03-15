package org.sapia.ubik.mcast.control;

import org.sapia.ubik.net.ServerAddress;

/**
 * A handler of {@link ControlRequest}s.
 * 
 * @author yduchesne
 * 
 */
public interface ControlRequestHandler {

  /**
   * Handles the given request.
   * 
   * @param originNode
   *          the node from which the request originates.
   * @param originAddress 
   *          the unicast {@link ServerAddress} of the origin node.
   * @param request
   *          a {@link ControlRequest}.
   */
  public void handle(String originNode, ServerAddress originAddress, ControlRequest request);

}