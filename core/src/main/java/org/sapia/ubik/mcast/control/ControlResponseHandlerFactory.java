package org.sapia.ubik.mcast.control;

import java.util.Set;

import org.sapia.ubik.mcast.control.challenge.ChallengeResponseHandler;
import org.sapia.ubik.mcast.control.heartbeat.HeartbeatResponseHandler;

/**
 * A factory of {@link ControlResponseHandler}s.
 * 
 * @author yduchesne
 * 
 */
class ControlResponseHandlerFactory {

  /**
   * @param callback
   *          the {@link ChannelCallback}.
   * @param targetedNodes
   *          a {@link Set} containing the identifiers of the nodes that were
   *          targeted.
   * @return a {@link ControlResponseHandler}.
   */
  static ControlResponseHandler createHeartbeatResponseHandler(ControllerContext context, Set<String> targetedNodes) {
    return new HeartbeatResponseHandler(context, targetedNodes);
  }

  /**
   * @param callback
   *          the {@link ChannelCallback}.
   * @param targetedNodes
   *          a {@link Set} containing the identifiers of the nodes that were
   *          targeted.
   * @return a {@link ControlResponseHandler}.
   */
  static ControlResponseHandler createChallengeResponseHandler(ControllerContext context, Set<String> targetedNodes) {
    return new ChallengeResponseHandler(context, targetedNodes);
  }

}
