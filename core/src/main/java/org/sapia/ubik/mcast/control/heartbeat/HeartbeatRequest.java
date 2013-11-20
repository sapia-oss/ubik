package org.sapia.ubik.mcast.control.heartbeat;

import java.util.Set;

import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Clock;

/**
 * An instance of this class is cascaded from the master node to its slaves in
 * order for them to report their heartbeat.
 * 
 * @see HeartbeatRequestHandler
 * @see HeartbeatResponse
 * 
 * @author yduchesne
 * 
 */
public class HeartbeatRequest extends ControlRequest {

  /**
   * Meant for externalization only.
   */
  public HeartbeatRequest() {
  }

  private HeartbeatRequest(Set<String> targetedNodes) {
    super(targetedNodes);
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
  public HeartbeatRequest(Clock clock, long requestId, String masterNode, ServerAddress masterAddress, Set<String> targetedNodes) {
    super(clock, requestId, masterNode, masterAddress, targetedNodes);
  }

  @Override
  protected ControlRequest getCopy(Set<String> targetedNodes) {
    return new HeartbeatRequest(targetedNodes);
  }

}
