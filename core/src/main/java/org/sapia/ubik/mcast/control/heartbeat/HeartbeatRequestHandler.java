package org.sapia.ubik.mcast.control.heartbeat;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.mcast.control.ControlRequestHandler;
import org.sapia.ubik.mcast.control.ControlResponseFactory;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.util.PropertiesUtil;

/**
 * This class encapsulates the logic for handling {@link HeartbeatRequest}s.
 * 
 * @author yduchesne
 * 
 */
public class HeartbeatRequestHandler implements ControlRequestHandler {

  /**
   * Property used in development to test handling of unresponding nodes. Do not
   * use otherwise.
   */
  private static final String IGNORE_HEARTBEAT_REQUEST = "ubik.rmi.naming.mcast.heartbeat.request.ignore";

  private Category log = Log.createCategory(getClass());
  private ControllerContext context;
  private boolean ignoreHeartbeatRequest = false;

  /**
   * @param context
   *          the {@link ControllerContext}.
   */
  public HeartbeatRequestHandler(ControllerContext context) {
    this.context = context;
    ignoreHeartbeatRequest = PropertiesUtil.isSystemPropertyTrue(IGNORE_HEARTBEAT_REQUEST);
  }

  /**
   * @param originNode
   *          the node from which the request has been cascaded.
   * @param request
   *          a {@link ControlRequest} (expected to be a
   *          {@link HeartbeatRequest}).
   */
  @Override
  public void handle(String originNode, ControlRequest request) {
    if (ignoreHeartbeatRequest) {
      if (context.getRole() == Role.MASTER) {
        log.debug("Received heartbeat request from other master node %s, triggering challenge", originNode);
        context.triggerChallenge();
      }
    } else {
      context.heartbeatRequestReceived();
      context.getChannelCallback().heartbeat(originNode, request.getMasterAddress());
      context.getChannelCallback().sendResponse(request.getMasterNode(),
          ControlResponseFactory.createHeartbeatResponse(request, context.getChannelCallback().getAddress()));
      if (context.getRole() == Role.MASTER) {
        log.debug("Received heartbeat request from other master node %s, triggering challenge", originNode);
        context.triggerChallenge();
      } else {
        context.setMasterNode(request.getMasterNode());
      }
    }
  }

}
