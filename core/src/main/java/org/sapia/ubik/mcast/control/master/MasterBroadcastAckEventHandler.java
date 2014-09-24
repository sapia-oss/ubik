package org.sapia.ubik.mcast.control.master;

import java.util.List;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControlEvent;
import org.sapia.ubik.mcast.control.ControlEventHandler;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.net.ServerAddress;

/**
 * Handles {@link MasterBroadcastAckEvent}s.
 * 
 * @author yduchesne
 *
 */
public class MasterBroadcastAckEventHandler implements ControlEventHandler {
  
  private Category log = Log.createCategory(getClass());
  
  private ControllerContext context;
  
  public MasterBroadcastAckEventHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ServerAddress originAddress,
      ControlEvent event) {
    MasterBroadcastAckEvent ack = (MasterBroadcastAckEvent) event;
    
    // if a ack is received, it may be from a node that doesn't have this 
    // node in its view (or as a master).
    // In such a case, we're forcing a resync of that node
    if (!context.getChannelCallback().containsNode(originNode)) {
      List<NodeInfo> view = context.getChannelCallback().getView();
      log.info("Received master broadcast ack from %s. Node is not in view, replying with master sync event", originNode);
      MasterSyncEvent sync = new MasterSyncEvent(view);
      context.getChannelCallback().sendUnicastEvent(originAddress, sync);
      
    // slave node does not have same number of nodes as master, forcing resync by adding to purgatory  
    } else if (ack.getNodeCount() != context.getChannelCallback().getNodeCount()) {
      List<NodeInfo> view = context.getChannelCallback().getView();
      log.info("Received master broadcast ack from %s: number of nodes inconsistent with master view. Replying with master sync event", originNode);
      view.add(new NodeInfo(context.getChannelCallback().getAddress(), context.getChannelCallback().getNode()));
      MasterSyncEvent sync = new MasterSyncEvent(view);
      context.getChannelCallback().sendUnicastEvent(originAddress, sync);
    } else {
      log.info("Received master broadcast ack from %s. Nothing to do: number of nodes consistent with master view: %s", originNode, ack.getNodeCount());           
    }
  }


}
