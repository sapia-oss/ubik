package org.sapia.ubik.mcast.control.gossip;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlEvent;
import org.sapia.ubik.mcast.control.ControlEventHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.net.ServerAddress;

/**
 * Handles {@link GossipSyncAckControlEvent}s: these are sent by member nodes that
 * have been contacted to exchange cluster view information. Such members each reply
 * with a {@link GossipSyncAckControlEvent}.
 * 
 * @author yduchesne
 *
 */
public class GossipSyncAckControlEventHandler implements ControlEventHandler {
  
  private Category log = Log.createCategory(getClass());
  private ControllerContext context;

  public GossipSyncAckControlEventHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ServerAddress originAddress, ControlEvent event) {
    GossipSyncAckControlEvent syncEvent = (GossipSyncAckControlEvent) event;
    log.trace("Received GossipSyncAckControlEvent from %s @ %s", originNode, originAddress);
    context.getMetrics().incrementCounter("eventController.onSyncGossipAck");

    for (NodeInfo n : syncEvent.getView()) {
      if (!context.getNode().equals(n.getNode())) {
        context.getEventChannel().addNewNode(n.getNode(), n.getAddr());
      }
    }
    context.getEventChannel().heartbeat(originNode, originAddress);
  }

}
