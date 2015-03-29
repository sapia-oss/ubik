package org.sapia.ubik.mcast.control.heartbeat;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.mcast.control.ControlRequestHandler;
import org.sapia.ubik.mcast.control.ControlResponseFactory;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.net.ServerAddress;

/**
 * This class encapsulates the logic for handling {@link HeartbeatRequest}s.
 * 
 * @author yduchesne
 * 
 */
public class HeartbeatRequestHandler implements ControlRequestHandler {

  private Category log = Log.createCategory(getClass());
  private ControllerContext context;

  /**
   * @param context
   *          the {@link ControllerContext}.
   */
  public HeartbeatRequestHandler(ControllerContext context) {
    this.context = context;
  }

  /**
   * @param originNode
   *          the node from which the request has been cascaded.
   * @param originAddress
   *          the unicast {@link ServerAddress} of the origin node.
   * @param request
   *          a {@link ControlRequest} (expected to be a
   *          {@link HeartbeatRequest}).
   */
  @Override
  public void handle(String originNode, ServerAddress originAddress, ControlRequest request) {
    log.info("Receiving heartbeat request from: %s", originNode);
    if (context.getConfig().isIgnoreHeartbeatRequests()) {
      if (context.getRole() == Role.MASTER) {
        log.info("Received heartbeat request from other master node %s, triggering challenge", originNode);
        context.triggerChallenge();
      }
    } else {
      context.heartbeatRequestReceived();
      context.getChannelCallback().heartbeatRequest(originNode, request.getMasterAddress());
      HeartbeatRequest req = (HeartbeatRequest) request;
      long pause = req.getPauseBeforeReply().getRandomTime().getValueInMillis();
      if (pause > 0) {
        log.debug("Pausing for %s millis before sending heartbeat response", req.getPauseBeforeReply());
        try {
          Thread.sleep(pause);
        } catch (InterruptedException e) {
          throw new org.sapia.ubik.concurrent.RuntimeInterruptedException("Thread interrupted while pausing",  e);
        }
      }
      log.debug("Sending back heartbeat response to: %s", req.getMasterAddress());
      context.getChannelCallback().sendResponse(request.getMasterNode(),
          ControlResponseFactory.createHeartbeatResponse(request, context.getChannelCallback().getAddress()));
      if (context.getRole() == Role.MASTER) {
        log.info("Received heartbeat request from other master node %s, triggering challenge", request.getMasterAddress());
        context.triggerChallenge();
      } else {
        context.setMasterNode(request.getMasterNode());
      }
    }
  }

}
