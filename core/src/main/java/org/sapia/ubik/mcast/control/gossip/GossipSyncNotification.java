package org.sapia.ubik.mcast.control.gossip;

import java.util.List;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.GossipNotification;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Condition;

/**
 * A notification that is sent by a host to initiate the exchange of cluster views.
 * 
 * @author yduchesne
 * 
 */
public class GossipSyncNotification extends GossipNotification {
  
  public static Condition<NodeInfo> NON_SUSPECT_NODES_FILTER = new Condition<NodeInfo>() {
    @Override
    public boolean apply(NodeInfo node) {
       return node.getState().isNormal();
    }
  };

    
  /**
   * DO NOT CALL: meant for externalization only.
   */
  public GossipSyncNotification() {
  }
  
  /**
   * @param sender the {@link ServerAddress} of the sender.
   * @param nodes the {@link List} of {@link NodeInfo} instance.
   */
  public GossipSyncNotification(List<NodeInfo> view) {
    super(view);
  }
}
