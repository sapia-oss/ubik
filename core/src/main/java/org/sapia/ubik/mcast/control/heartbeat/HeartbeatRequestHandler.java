package org.sapia.ubik.mcast.control.heartbeat;

import org.sapia.ubik.mcast.control.ControlRequest;
import org.sapia.ubik.mcast.control.ControlRequestHandler;
import org.sapia.ubik.mcast.control.ControlResponseFactory;
import org.sapia.ubik.mcast.control.ControllerContext;

/**
 * This class encapsulates the logic for handling {@link HeartbeatRequest}s.
 * 
 * @author yduchesne
 *
 */
public class HeartbeatRequestHandler implements ControlRequestHandler {
	
	private ControllerContext context;
	
	/**
	 * @param context the {@link ControllerContext}.
	 */
	public HeartbeatRequestHandler(ControllerContext context) {
	  this.context = context;
  }
	
	/**
	 * @param originNode the node from which the request has been
	 * cascaded.
	 * @param request a {@link ControlRequest} (expected to be a {@link HeartbeatRequest}).
	 */
	@Override
	public void handle(String originNode, ControlRequest request) {
		context.heartbeatRequestReceived();
		context.getChannelCallback().heartbeat(originNode, request.getMasterAddress());
		context.getChannelCallback().sendResponse(
				request.getMasterNode(), 
				ControlResponseFactory.createHeartbeatResponse(request, context.getChannelCallback().getAddress())
		);
	}

}
