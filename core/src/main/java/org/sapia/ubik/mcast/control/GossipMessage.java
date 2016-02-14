package org.sapia.ubik.mcast.control;

import java.util.List;

import org.sapia.ubik.mcast.NodeInfo;

/**
 * Specifies behavior common to gossip messages.
 * 
 * @author yduchesne
 */
public interface GossipMessage {

  /**
   * @return the {@link List} of {@link NodeInfo} instances corresponding to the nodes seen by another member.
   */
  public List<NodeInfo> getView();
  
}
