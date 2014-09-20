package org.sapia.ubik.mcast.control.heartbeat;

import org.sapia.ubik.mcast.control.ControlResponseFactory;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SynchronousControlRequest;
import org.sapia.ubik.mcast.control.SynchronousControlRequestHandler;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;

public class PingRequestHandler implements SynchronousControlRequestHandler {

  private ControllerContext context;

  public PingRequestHandler(ControllerContext context) {
    this.context = context;
  }

  @Override
  public SynchronousControlResponse handle(String originNode, SynchronousControlRequest request) {
    context.heartbeatRequestReceived();
    return ControlResponseFactory.createPingResponse(context.getNode());
  }

}
