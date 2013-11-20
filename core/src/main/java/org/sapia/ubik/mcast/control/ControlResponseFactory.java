package org.sapia.ubik.mcast.control;

import org.sapia.ubik.mcast.control.challenge.ChallengeResponse;
import org.sapia.ubik.mcast.control.heartbeat.HeartbeatResponse;
import org.sapia.ubik.mcast.control.heartbeat.PingResponse;
import org.sapia.ubik.net.ServerAddress;

/**
 * A factory of {@link ControlResponse}s.
 * 
 * @author yduchesne
 * 
 */
public class ControlResponseFactory {

  /**
   * @param code
   *          the response {@link Code} of the challenge response to return.
   * @param request
   *          the {@link ControlRequest} for which to create a response.
   * @param address
   *          the unicast {@link ServerAddress} of the node that is sending the
   *          response. s * @return a new {@link ChallengeResponse}.
   */
  public static ControlResponse createChallengeResponse(ChallengeResponse.Code code, ControlRequest request, ServerAddress address) {
    ChallengeResponse response = new ChallengeResponse(request.getRequestId(), code, address);
    return response;
  }

  /**
   * @param request
   *          the {@link ControlRequest} for which to create a response.
   * @param address
   *          the unicast {@link ServerAddress} of the node that is sending the
   *          response.
   * @return a new {@link HeartbeatResponse}.
   */
  public static ControlResponse createHeartbeatResponse(ControlRequest request, ServerAddress address) {
    return new HeartbeatResponse(request.getRequestId(), address);
  }

  /**
   * @param originNode
   *          the node from which the ping response is sent.
   * @return a new {@link PingResponse}
   */
  public static SynchronousControlResponse createPingResponse(String originNode) {
    return new PingResponse(originNode);
  }

}
