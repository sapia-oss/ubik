package org.sapia.ubik.mcast.control.master;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.mcast.control.ControlEvent;
import org.sapia.ubik.mcast.control.ControlEventHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.net.ServerAddress;

/**
 * Handles {@link MasterBroadcastEvent}s.
 * 
 * @author yduchesne
 *
 */
public class MasterBroadcastEventHandler implements ControlEventHandler {

  private Category log = Log.createCategory(getClass());

  private ControllerContext context;
  
  /**
   * @param context the {@link ControllerContext}.
   */
  public MasterBroadcastEventHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ServerAddress address, ControlEvent event) {
    MasterBroadcastEvent data = (MasterBroadcastEvent) event;
    String thisMaster = context.getMasterNode();

    // if the broadcast comes from a node that's not in this instance's
    // view, or if this node's current master (if it exists) does not corresponds 
    // to the node that sent the broadcast, we're acking that broadcast.
    if (context.getRole() == Role.MASTER || context.getChannelCallback().addNewNode(originNode, address)
        || (thisMaster != null && !thisMaster.equals(originNode))) {

      // if this node is itself a master, trigger a challenge
      if (context.getRole() == Role.MASTER) {
        log.info("Receive master broadcast from another node (%s). Triggering challenge", originNode);
        context.triggerChallenge();
      } else {
        log.info("Acking master broadcast from %s (current node does not have same master, or has no master yet)", originNode);
        context.getChannelCallback().sendUnicastEvent(address, new MasterBroadcastAckEvent(context.getChannelCallback().getNodeCount()));
      }
    
    // if previous condition not true, checking if this node has same node count as master 
    // (if yes, send ack to eventually trigger resync).
    } else if (context.getChannelCallback().getNodeCount() != data.getNodeCount()) {
      log.info("Acking master broadcast from %s (current node does not have same number of nodes as master)", originNode);
      context.getChannelCallback().sendUnicastEvent(address, new MasterBroadcastAckEvent(context.getChannelCallback().getNodeCount()));
    }
  }
  
  
}
