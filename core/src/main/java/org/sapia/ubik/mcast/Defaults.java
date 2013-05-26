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
  public static final int DEFAULT_UDP_PACKET_SIZE           = 3072;
  
  /**
   * The default sender count (see {@link Consts#MCAST_SENDER_COUNT}).
   */
  public static final int DEFAULT_SENDER_COUNT              = 3;
  
  /**
   * The default number of worker threads for unicast dispatchers.
   * 
   * (see {@link Consts#MCAST_HANDLER_COUNT}).
   */
  public static final int DEFAULT_HANDLER_COUNT             = 3;
  
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
  public static final int  DEFAULT_HEARTBEAT_INTERVAL       = 60000;
  
  /**
   * The interval at which a node will resync itself with the cluster by rebroadcasting its presence.
   * @see #DEFAULT_RESYNC_NODE_COUNT
   * @see Consts#MCAST_RESYNC_INTERVAL
   */
  public static final int  DEFAULT_RESYNC_INTERVAL          = 60000;
  
  /**
   * The default minimum number of nodes in the cluster before a given node will trigger auto-resync. 
   * 
   * @see #DEFAULT_RESYNC_INTERVAL
   * @see Consts#MCAST_RESYNC_NODE_COUNT
   */
  public static final int  DEFAULT_RESYNC_NODE_COUNT        = 0;
  
  /**
   * The default timeout for channel control responses. 
   */
  public static final long DEFAULT_CONTROL_RESPONSE_TIMEOUT = 60000;  
  
  /**
   * The size for the splits of control requests/notifications.
   */
  public static final int DEFAULT_CONTROL_SPLIT_SIZE        = 5;
  
  /**
   * The default number of maximum client connections for each remote peers.
   * 
   * @see Consts#MCAST_MAX_CLIENT_CONNECTIONS
   */
  public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 3;

}
