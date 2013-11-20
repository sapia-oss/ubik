package org.sapia.ubik.mcast.control;

/**
 * An instance of this interface is notified upon changes in the state of an
 * {@link EventChannelController}.
 * 
 * @author yduchesne
 * 
 */
public interface EventChannelControllerListener {

  /**
   * @param context
   *          the {@link ControllerContext} of the
   *          {@link EventChannelController} whose state has changed.
   */
  public void onChallengeCompleted(ControllerContext context);

  /**
   * @param context
   *          the {@link ControllerContext} of the
   *          {@link EventChannelController} whose state has chanded.
   * @param expectedCount
   *          the number of nodes that were expected to respond.
   * @param effectiveCount
   *          the number of nodes that effectively responsed.
   */
  public void onHeartBeatCompleted(ControllerContext context, int expectedCount, int effectiveCount);

}
