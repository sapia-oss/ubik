package org.sapia.ubik.mcast.control;

import org.sapia.ubik.net.ServerAddress;

/**
 * A handler of {@link SynchronousControlRequest}s.
 * 
 * @author yduchesne
 * 
 */
public interface SynchronousControlRequestHandler {

  /**
   * Handles the given request.
   * 
   * @param originNode
   *          the node from which the request originates.
   * @param originAddress
   *          the unicast {@link ServerAddress} of the orginin node.
   * @param request
   *          a {@link SynchronousControlRequest}.
   * @return a {@link SynchronousControlResponse}, or <code>null</code> if no
   *         response is internally created.
   */
  public SynchronousControlResponse handle(String originNode, ServerAddress originAddress, SynchronousControlRequest request);

}