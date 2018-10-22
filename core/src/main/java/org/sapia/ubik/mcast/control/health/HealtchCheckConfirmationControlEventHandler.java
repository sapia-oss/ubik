package org.sapia.ubik.mcast.control.health;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlEvent;
import org.sapia.ubik.mcast.control.ControlEventHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.net.ServerAddress;

/**
 * Handles {@link HealthCheckConfirmationControlEvent}s: updates "own" state for suspect node,
 * based on status made by third-part node(s).
 * 
 * @see DelegatedHealthCheckControlEventHandler
 * 
 * @author yduchesne
 *
 */
public class HealtchCheckConfirmationControlEventHandler implements ControlEventHandler {
  
  private Category log = Log.createCategory(getClass());
  
  private ControllerContext context;
  
  /**
   * @param context the {@link ControllerContext} to use.
   */
  public HealtchCheckConfirmationControlEventHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ServerAddress originAddress, ControlEvent event) {
    HealthCheckConfirmationControlEvent confirmation = (HealthCheckConfirmationControlEvent) event;
    NodeInfo                            suspect      = context.getEventChannel().getNodeInfoFor(confirmation.getSuspect().getNode());
    
    context.getMetrics().incrementCounter("eventController.onDelegatedHealthCheckConfirmation");
    context.getEventChannel().heartbeat(originNode, originAddress);

    // might have been removed already
    if (suspect != null) {
  	  if (confirmation.isUp()) {
  	    log.info("Healtch check successful on node %s", suspect);
  	    suspect.reset(context.getClock());
  	  } else {
  	    log.info("Healtch check failed on node %s (removing from view)", suspect);
  	    context.getEventChannel().down(confirmation.getSuspect().getNode());
  	  }
    }
  }

}
