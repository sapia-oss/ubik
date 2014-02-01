package org.sapia.ubik.net;

/**
 * Specifies callbacks that are invoked based on the state of a connection.
 * 
 * @author yduchesne
 *
 */
public interface ConnectionStateListener {
  
  /**
   * Invoked when an connection occurs.
   */
  public void onConnected();
  
  /**
   * Invoked when an connection occurs after a disconnection.
   */
  public void onReconnected();

  /**
   * Invoked when an disconnection occurs.
   */
  public void onDisconnected();

}
