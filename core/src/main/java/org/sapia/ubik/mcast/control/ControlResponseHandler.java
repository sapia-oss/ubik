package org.sapia.ubik.mcast.control;

/**
 * A handler of {@link ControlResponse}s.
 * 
 * @author yduchesne
 * 
 */
public interface ControlResponseHandler {

  /**
   * This method returns <code>true</code> if this instance can handle the given
   * request.
   * 
   * @param response
   *          the {@link ControlResponse} to check.
   * @return <code>true</code> if the given {@link ControlResponse} is accepted
   *         by this instance.
   */
  public boolean accepts(ControlResponse response);

  /**
   * This method is called only if this instance accepts the given response (see
   * {@link #accepts(ControlResponse)}). The method returns <code>true</code> if
   * all expected responses have been received, in which case this instance is
   * discarded.
   * 
   * @param originNode
   *          the identifier of the node from which the response originates.
   * @param response
   *          a {@link ControlResponse}.
   * @return <code>true</code> if this instance as finished receiving the
   *         responses it expected.
   */
  public boolean handle(String originNode, ControlResponse response);

  /**
   * This method is called when this instance has not completed (see
   * {@link #handle(String, ControlResponse)}) within a specified timeframe (see
   * {@link ControllerConfiguration#getResponseTimeout()}).
   */
  public void onResponseTimeOut();

}