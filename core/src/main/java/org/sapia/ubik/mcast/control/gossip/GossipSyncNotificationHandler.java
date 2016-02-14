package org.sapia.ubik.mcast.control.gossip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.ControllerContext;
import org.sapia.ubik.mcast.control.GossipNotification;
import org.sapia.ubik.mcast.control.GossipNotificationHandler;
import org.sapia.ubik.net.ServerAddress;

/**
 * Sent to randomly chosen cluster member nodes, in order to start the
 * exchange of cluster member list data.
 * 
 * @author yduchesne
 *
 */
public class GossipSyncNotificationHandler implements GossipNotificationHandler {

  private Category          log       = Log.createCategory(getClass());
  private ControllerContext context;

  public GossipSyncNotificationHandler(ControllerContext context) {
    this.context = context;
  }
  
  @Override
  public void handle(String originNode, ServerAddress originAddress, GossipNotification notif) {
    GossipSyncNotification syncNotif = (GossipSyncNotification) notif;
    log.debug("Received GossipSyncNotification from %s @ %s", originNode, originAddress);
    Set<NodeInfo> received = new HashSet<>(syncNotif.getView().size() + 1);
    for (NodeInfo n : syncNotif.getView()) {
      if (!n.getNode().equals(context.getNode())) {
        context.getEventChannel().addNewNode(n.getNode(), n.getAddr());
        received.add(n);
      }
    }
    context.getEventChannel().heartbeat(originNode, originAddress);
    
    List<NodeInfo> toSend = new ArrayList<>();
    for (NodeInfo n : context.getEventChannel().getView(GossipSyncNotification.NON_SUSPECT_NODES_FILTER)) {
      if (!received.contains(n)) {
        toSend.add(n);
      }
    }
    context.getEventChannel().sendUnicastEvent(
        originAddress, new GossipSyncAckControlEvent(toSend)
    );
  }
}
