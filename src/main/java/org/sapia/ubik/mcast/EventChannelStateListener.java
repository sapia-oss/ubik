package org.sapia.ubik.mcast;

import org.sapia.ubik.net.ServerAddress;

public interface EventChannelStateListener {

  /**
   * @author yduchesne
   *
   */
  public static class EventChannelEvent {
    
    /**
     * The identifier of the origin node.
     */
    private String node;
    
    /**
     * The {@link ServerAddress} of the origin node.
     */
    private ServerAddress address;
    
    /**
     * @param node the identifier of the origin node.
     * @param address the unicast {@link ServerAddress} of the origin node.
     */
    EventChannelEvent(String node, ServerAddress address) {
      this.node    = node;
      this.address = address;
    }
    
    /**
     * @return the unicast {@link ServerAddress} of the origin node.
     */
    public ServerAddress getAddress() {
      return address;
    }
    
    /**
     * @return the identifier of the origin node.
     */
    public String getNode() {
      return node;
    }
  }
  
  /**
   * Called when a remote node has been deemed disappeared.
   * 
   * @param event an {@link EventChannelEvent} holding the unique identifier of 
   * the node that was detected as having disappeared.
   */
  public void onDown(EventChannelEvent event);
  
  /**
   * Called when a remote node has been discovered.
   * 
   * @param event an {@link EventChannelEvent} holding the unique identifier of 
   * the node that was discovered.
   */
  public void onUp(EventChannelEvent event);
}
