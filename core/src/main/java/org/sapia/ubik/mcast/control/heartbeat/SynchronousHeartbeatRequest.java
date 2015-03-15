package org.sapia.ubik.mcast.control.heartbeat;

import java.util.Set;

import org.sapia.ubik.mcast.control.SplitteableSynchronousControlRequest;

/**
 * A synchronous heartbeat request.
 * 
 * @author yduchesne
 *
 */
public class SynchronousHeartbeatRequest  extends SplitteableSynchronousControlRequest {
  
  static final long serialVersionUID = 1L;
  
  public SynchronousHeartbeatRequest() {
  }
  
  public SynchronousHeartbeatRequest(Set<String> targetedNodes) {
    super(targetedNodes);
  }

  
  @Override
  protected SplitteableSynchronousControlRequest getCopy(Set<String> targetedNodes) {
    SplitteableSynchronousControlRequest req = new SynchronousHeartbeatRequest(targetedNodes);
    return req;
  }

}
