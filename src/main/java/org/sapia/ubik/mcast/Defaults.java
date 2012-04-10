package org.sapia.ubik.mcast;

import org.sapia.ubik.rmi.Consts;

/**
 * Holds default values that are shared across different {@link BroadcastDispatcher} and {@link UnicastDispatcher} 
 * implementations. Also holds value for an {@link EventChannel}.
 * 
 * @author yduchesne
 *
 */
public class Defaults {

  /**
   * The default UDP packet size (see {@link Consts#MCAST_BUFSIZE_KEY}).
   */
  public static final int DEFAULT_UDP_PACKET_SIZE           = 4096;
  
  /**
   * The default sender count (see {@link Consts#MCAST_SENDER_COUNT}).
   */
  public static final int DEFAULT_SENDER_COUNT              = 3;
  
  /**
   * The default synchronous response timeout (see {@link Consts#MCAST_SYNC_RESPONSE_TIMEOUT}).
   */
  public static final int DEFAULT_SYNC_RESPONSE_TIMEOUT     = 10000;
  
  /**
   * The default heartbeat timeout (see {@link Consts#MCAST_HEARTBEAT_TIMEOUT}).
   */
  public static final long DEFAULT_HEARTBEAT_TIMEOUT        = 90000;
  
  /**
   * The default heartbeat interval (see {@link Consts#MCAST_HEARTBEAT_INTERVAL}).
   */
  public static final int  DEFAULT_HEARTBEAT_INTERVAL       = 40000;
  
  /**
   * The default timeout for channel control responses. 
   */
  public static final long DEFAULT_CONTROL_RESPONSE_TIMEOUT = 60000;  
  
  /**
   * The size for the batches of control requests/notifications.
   */
  public static final int DEFAULT_CONTROL_BATCH_SIZE        = 5;
}
