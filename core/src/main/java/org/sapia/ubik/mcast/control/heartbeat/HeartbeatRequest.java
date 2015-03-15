package org.sapia.ubik.mcast.control.heartbeat;

import java.util.Set;

import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.TimeRange;

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
  
  static final long serialVersionUID = 1L;

  private TimeRange pauseBeforeReply;
  
  /**
   * Meant for externalization only.
   */
  public HeartbeatRequest() {
  }
  
  /**
   * @param clock
   *          the {@link SysClock} to use.
   * @param requestId
   *          the identifier to assign to this request.
   * @param masterNode
   *          the master node's identifier.
   * @param masterAddress
   *          the unicast {@link ServerAddress} of the master node.
   * @param targetedNodes
   *          the slave nodes that are targeted.
   * @param pauseBeforeReply 
   *          the pause to observe before sending back a {@link HeartbeatResponse} - if the returned value <= 0, then
   *          no pause should be observed.
   */
  public HeartbeatRequest(SysClock clock, long requestId, String masterNode, ServerAddress masterAddress, Set<String> targetedNodes, TimeRange pauseBeforeReply) {
    super(clock, requestId, masterNode, masterAddress, targetedNodes);
    this.pauseBeforeReply = pauseBeforeReply;
  }
  
  /**
   * @param creation time.
   *          the creation time, in millis.
   * @param requestId
   *          the identifier to assign to this request.
   * @param masterNode
   *          the master node's identifier.
   * @param masterAddress
   *          the unicast {@link ServerAddress} of the master node.
   * @param targetedNodes
   *          the slave nodes that are targeted.
   * @param pauseBeforeReply 
   *          the pause to observe before sending back a {@link HeartbeatResponse} - if the returned value <= 0, then
   *          no pause should be observed.
   */
  public HeartbeatRequest(long creationTime, long requestId, String masterNode, ServerAddress masterAddress, Set<String> targetedNodes, TimeRange pauseBeforeReply) {
    super(creationTime, requestId, masterNode, masterAddress, targetedNodes);
    this.pauseBeforeReply = pauseBeforeReply;
  }
  
  /**
   * @return the pause to observe before sending back a {@link HeartbeatResponse} - if the returned value <= 0, then
   * no pause should be observed.
   */
  public TimeRange getPauseBeforeReply() {
    return pauseBeforeReply;
  }

  @Override
  protected ControlRequest getCopy(Set<String> targetedNodes) {
    return new HeartbeatRequest(getCreationTime(), getRequestId(), getMasterNode(), getMasterAddress(), targetedNodes, pauseBeforeReply);
  }

}
