package org.sapia.ubik.mcast.control.heartbeat;

import org.sapia.ubik.mcast.control.ControlResponseFactory;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SynchronousControlRequest;
import org.sapia.ubik.mcast.control.SynchronousControlRequestHandler;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;

/**
 * A {@link SynchronousControlRequestHandler} for {@link PingRequest}s.
 * 
 * @author yduchesne
 *
 */
public class PingRequestHandler implements SynchronousControlRequestHandler {

  private ControllerContext context;

  /**
   * @param context the {@link ControllerContext} corresponding to the event channel in the context of which
   * this instance is used.
   */
  public PingRequestHandler(ControllerContext context) {
    this.context = context;
  }

  @Override
  public SynchronousControlResponse handle(String originNode, ServerAddress addr, SynchronousControlRequest request) {
    //context.heartbeatRequestReceived();
    context.getChannelCallback().resync();
    return ControlResponseFactory.createPingResponse(context.getNode(), context.getChannelCallback().getAddress());
  }

}
