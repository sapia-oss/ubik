package org.sapia.ubik.mcast.control.challenge;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.mcast.control.ControlRequestHandler;
import org.sapia.ubik.mcast.control.ControlResponseFactory;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.challenge.ChallengeResponse.Code;

/**
 * A handler of {@link ChallengeRequest}s.
 * 
 * @author yduchesne
 * 
 */
public class ChallengeRequestHandler implements ControlRequestHandler {

  private Category log = Log.createCategory(getClass());

  private ControllerContext context;

  public ChallengeRequestHandler(ControllerContext context) {
    this.context = context;
  }

  /**
   * This method checks of this node is currently {@link Role#MASTER} or
   * {@link Role#MASTER_CANDIDATE}. If so, the challenge fails. Otherwise, the
   * challenge succeeds. A response to either effect is sent to the node
   * currently attempting to become the master.
   * 
   * @see ControlRequestHandler#handle(String, ControlRequest)
   */
  @Override
  public void handle(String originNode, ControlRequest request) {
    log.info("Received challenge request from %s (master is %s)", originNode, request.getMasterNode());
    context.challengeRequestReceived();
    context.getChannelCallback().heartbeatRequest(originNode, request.getMasterAddress());
    if (context.getRole() == Role.MASTER || context.getRole() == Role.MASTER_CANDIDATE) {
      log.info("This node's status is currently %s. The challenge fails", context.getRole());
      context.getChannelCallback().sendResponse(request.getMasterNode(),
          ControlResponseFactory.createChallengeResponse(Code.DENIED, request, context.getChannelCallback().getAddress()));
    } else {
      log.info("Challenge accepted by node %s. Sending response to %s", context.getNode(), request.getMasterNode());
      context.setRole(Role.SLAVE);
      context.getChannelCallback().sendResponse(request.getMasterNode(),
          ControlResponseFactory.createChallengeResponse(Code.ACCEPTED, request, context.getChannelCallback().getAddress()));
    }
  }

}
