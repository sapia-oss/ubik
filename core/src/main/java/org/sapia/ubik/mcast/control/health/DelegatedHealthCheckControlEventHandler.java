package org.sapia.ubik.mcast.control.health;

import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.control.ControlEvent;
import org.sapia.ubik.mcast.control.ControlEventHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;

/**
 * Handles {@link DelegatedHealthCheckControlEvent}s, which are sent by nodes that need performing
 * health checks on suspect nodes. This delegation mechanism ensures that network issues at the origin
 * nodes won't prevent health-checking of the suspect nodes.
 * <p>
 * An instance of this class will send a {@link HealthCheckConfirmationControlEvent} for any health incoming event,
 * indicating the output of testing the suspect node.
 * 
 * @author yduchesne
 *
 */
public class DelegatedHealthCheckControlEventHandler implements ControlEventHandler {
  
  private Category log = Log.createCategory(getClass());
  
  private ControllerContext context;
  
  public DelegatedHealthCheckControlEventHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ServerAddress originAddress, ControlEvent event) {
    DelegatedHealthCheckControlEvent healtchCheckEvent = (DelegatedHealthCheckControlEvent) event;
    log.info("Received event for suspect node: %s", healtchCheckEvent.getTarget());
    context.getEventChannel().heartbeat(originNode, originAddress);
    try {
      Set<SynchronousControlResponse> responses = context.getEventChannel().sendSynchronousRequest(
          Collects.arrayToSet(healtchCheckEvent.getTarget().getNode()), 
          new SynchronousHealthCheckRequest(), 
          context.getConfig().getHealthCheckDelegateTimeout()
      );
      if (responses.isEmpty()) {
        log.info("Received no response for health check on %s. Sending confirmation", healtchCheckEvent.getTarget());
        context.getEventChannel().sendUnicastEvent(
            originAddress, 
            new HealthCheckConfirmationControlEvent(
                healtchCheckEvent.getTarget(), 
                false
            )
        );
      } else {
        log.info("Health check successful for %s. Sending confirmation", healtchCheckEvent.getTarget());
        context.getEventChannel().sendUnicastEvent(
            originAddress, 
            new HealthCheckConfirmationControlEvent(
                healtchCheckEvent.getTarget(), 
                true
            )
        );
      }
    } catch (Exception e) {
      log.error("Unexpected error caught during health check of %s (%s). Sending confirmation", 
          healtchCheckEvent.getTarget(), e.getMessage());
      context.getEventChannel().sendUnicastEvent(
          originAddress, 
          new HealthCheckConfirmationControlEvent(
              healtchCheckEvent.getTarget(), 
              false
          )
      );
    }
  }

}
