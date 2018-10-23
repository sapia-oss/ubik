package org.sapia.ubik.mcast.control.health;

import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SynchronousControlRequest;
import org.sapia.ubik.mcast.control.SynchronousControlRequestHandler;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;

/**
 * Handles {@link SynchronousHealthCheckRequest}s.
 * 
 * @author yduchesne
 *
 */
public class SynchronousHealthCheckRequestHandler implements SynchronousControlRequestHandler {
  
  private ControllerContext context;
  
  public SynchronousHealthCheckRequestHandler(ControllerContext context) {
    this.context = context;
  }

  @Override
  public SynchronousControlResponse handle(String originNode, ServerAddress originAddress,
      SynchronousControlRequest request) {
    context.getEventChannel().heartbeat(originNode, originAddress);
    context.getMetrics().incrementCounter("eventController.onSyncHealthCheck");
    return new SynchronousHealthCheckResponse(
      context.getEventChannel().getNode(), context.getEventChannel().getAddress()
    );
  }
  
}
