package org.sapia.ubik.mcast.control.challenge;

import java.util.Set;

import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Clock;

/**
 * This request is sent by a node that attempts becoming the master.
 * 
 * @see ChallengeRequestHandler
 * @see ChallengeResponse
 * 
 * @author yduchesne
 * 
 */
public class ChallengeRequest extends ControlRequest {

  /**
   * Meant for externalization
   */
  public ChallengeRequest() {
  }

  /**
   * @param clock
   *          the {@link Clock} to use.
   * @param requestId
   *          the identifier to assign to this request.
   * @param masterNode
   *          the master node's identifier.
   * @param masterAddress
   *          the unicast {@link ServerAddress} of the master node.
   * @param targetedNodes
   *          the slave nodes that are targeted.
   */
  public ChallengeRequest(Clock clock, long requestId, String masterNode, ServerAddress masterAddress, Set<String> targetedNodes) {
    super(clock, requestId, masterNode, masterAddress, targetedNodes);
  }

  private ChallengeRequest(Set<String> targetedNodes) {
    super(targetedNodes);
  }

  @Override
  protected ControlRequest getCopy(Set<String> targetedNodes) {
    return new ChallengeRequest(targetedNodes);
  }
}
