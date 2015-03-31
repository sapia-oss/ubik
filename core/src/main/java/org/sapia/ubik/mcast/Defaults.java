package org.sapia.ubik.mcast;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.TimeValue;
import org.sapia.ubik.util.TimeRange;

/**
 * Holds default values that are shared across different
 * {@link BroadcastDispatcher} and {@link UnicastDispatcher} implementations.
 * Also holds value for an {@link EventChannel}.
 *
 * @author yduchesne
 *
 */
public class Defaults {

  /**
   * The default value for the batch size when looking up synchronously other JNDI nodes, from a
   * given node missing a stub (see {@link Consts#JNDI_SYNC_LOOKUP_BATCH_SIZE}).
   */
  public static final int DEFAULT_JNDI_SYNC_LOOKUP_BATCH_SIZE  = 5;

  /**
   * The default value of the timeout for client-side JNDI discovery. (see {@link Consts#JNDI_CLIENT_DISCO_TIMEOUT}).
   */

  public static final TimeValue DEFAULT_JNDI_CLIENT_DISCO_TIMEOUT = TimeValue.createMillis(5000);

  /**
   * The default value for the batch size when looking up synchronously other JNDI nodes, from a
   * given node missing a stub (see {@link Consts#JNDI_LAZY_LOOKUP_INTERVAL}).
   */
  public static final TimeValue DEFAULT_LAZY_LOOKUP_INTERVAL = TimeValue.createMillis(1000);

  /**
   * The default UDP packet size (see {@link Consts#MCAST_BUFSIZE_KEY}).
   */
  public static final int DEFAULT_UDP_PACKET_SIZE = 3072;

  /**
   * The default TTL for UDP multicast packets (see {@link Consts#MCAST_TTL}).
   */
  public static final int DEFAULT_TTL = 32;

  /**
   * The default sender count (see {@link Consts#MCAST_SENDER_COUNT}).
   */
  public static final int DEFAULT_SENDER_COUNT = 5;

  /**
   * The default number of worker threads for unicast and broadcast dispatchers.
   *
   * (see {@link Consts#MCAST_HANDLER_COUNT}).
   */
  public static final int DEFAULT_HANDLER_COUNT = 10;

  /**
   * The default queue size for unicast and broadcast dispatchers.
   *
   * (see {@link Consts#MCAST_HANDLER_QUEUE_SIZE}).
   */
  public static final int DEFAULT_HANDLER_QUEUE_SIZE = 100;

  
  /**
   * The default synchronous response timeout (see
   * {@link Consts#MCAST_SYNC_RESPONSE_TIMEOUT}).
   */
  public static final TimeValue DEFAULT_SYNC_RESPONSE_TIMEOUT = TimeValue.createMillis(10000);
  
  
  /**
   * The default synchronous response timeout (see
   * {@link Consts#MCAST_ASYNC_ACK_TIMEOUT}).
   */
  public static final TimeValue DEFAULT_ASYNC_ACK_TIMEOUT = TimeValue.createMillis(3000);
  

  /**
   * The default heartbeat timeout (see {@link Consts#MCAST_HEARTBEAT_TIMEOUT}).
   */
  public static final TimeValue DEFAULT_HEARTBEAT_TIMEOUT = TimeValue.createMillis(90000);
  
  /**
   * The default heartbeat response delay (see {@link Consts#MCAST_HEARTBEAT_RESPONSE_DELAY}).
   */
  public static final TimeRange DEFAULT_HEARTBEAT_RESPONSE_DELAY = TimeRange.valueOf("2000ms:3000ms");

  /**
   * The default heartbeat interval (see {@link Consts#MCAST_HEARTBEAT_INTERVAL}
   * ).
   */
  public static final TimeValue DEFAULT_HEARTBEAT_INTERVAL = TimeValue.createMillis(30000);

  /**
   * The interval at which a node will resync itself with the cluster by
   * rebroadcasting its presence.
   *
   * @see #DEFAULT_RESYNC_NODE_COUNT
   * @see Consts#MCAST_RESYNC_INTERVAL
   */
  public static final TimeValue DEFAULT_RESYNC_INTERVAL = TimeValue.createMillis(60000);

  /**
   * The default minimum number of nodes in the cluster before a given node will
   * trigger auto-resync.
   *
   * @see #DEFAULT_RESYNC_INTERVAL
   * @see Consts#MCAST_RESYNC_NODE_COUNT
   */
  public static final int DEFAULT_RESYNC_NODE_COUNT = 0;

  /**
   * The default number of nodes to send per force-resync event.
   *
   * @see Consts#MCAST_HEARTBEAT_FORCE_RESYNC_BATCH_SIZE
   */
  public static final int DEFAULT_FORCE_RESYNC_BATCH_SIZE = 3;

  /**
   * The default number of force-resync attempts.
   *
   * @see Consts#MCAST_HEARTBEAT_FORCE_RESYNC_ATTEMPTS
   */
  public static final int DEFAULT_FORCE_RESYNC_ATTEMPTS = 3;

  /**
   * The default timeout for channel control responses.
   */
  public static final TimeValue DEFAULT_CONTROL_RESPONSE_TIMEOUT = TimeValue.createMillis(60000);

  /**
   * The size for the splits of control requests/notifications.
   */
  public static final int DEFAULT_CONTROL_SPLIT_SIZE = 5;
  
  /**
   * The size for the splits of synchronous heartbeat requests/notifications.
   */
  public static final int DEFAULT_SYNC_HEARTBEAT_SPLIT_SIZE = 2;
  
  /**
   * @see Consts#MCAST_CONTROL_SYNC_HEARTBEAT_ENABLED
   */
  public static boolean DEFAULT_SYNC_HEARTBEAT_ENABLED = true;

  /**
   * @see Consts#MCAST_CONTROL_SYNC_HEARTBEAT_RESPONSE_TIMEOUT
   */
  public static TimeValue DEFAULT_SYNC_HEARTBEAT_RESPONSE_TIMEOUT = TimeValue.createMillis(10000);
  
  /**
   * The default number of maximum client connections for each remote peers.
   *
   * @see Consts#MCAST_MAX_CLIENT_CONNECTIONS
   */
  public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 3;

  /**
   * The default max number of ping attempts.
   *
   * @see Consts#MCAST_MAX_PING_ATTEMPTS
   */
  public static final int DEFAULT_PING_ATTEMPTS = 3;

  /**
   * The default ping interval.
   *
   * @see Consts#MCAST_PING_INTERVAL
   */
  public static final TimeValue DEFAULT_PING_INTERVAL = TimeValue.createMillis(2000);

  /**
   * The default master broadcast interval.
   *
   * @see Consts#MCAST_MASTER_BROADCAST_INTERVAL
   */
  public static final TimeValue DEFAULT_MASTER_BROADCAST_INTERVAL = TimeValue.createMillis(120000);

  /**
   * The default broadcast monitor reconnection interval.
   *
   * @see Consts#MCAST_BROADCAST_MONITOR_INTERVAL
   */
  public static final TimeValue DEFAULT_BROADCAST_MONITOR_INTERVAL = TimeValue.createMillis(30000);

  /**
   * The default random time range specifying the interval used by the event channel to publish itself
   * upon either upon resync, or as part of master broadcast.
   *
   * @see Consts#MCAST_CHANNEL_PUBLISH_INTERVAL
   */
  public static final TimeRange DEFAULT_CHANNEL_PUBLISH_INTERVAL = TimeRange.valueOf("1s-5s");

  /**
   * The default random time range specifying the delay observed by the event channel before publishing itself
   * for the first time at startup.
   *
   * @see Consts#MCAST_CHANNEL_START_DELAY
   *
   */
  public static final TimeRange DEFAULT_CHANNEL_START_DELAY = TimeRange.valueOf("500ms-3000ms");

  /**
   * The time range used to determine the default interval at which JNDI servers synchronize their state with others.
   *
   * @see Consts#JNDI_SYNC_INTERVAL
   */
  public static final TimeRange DEFAULT_JNDI_SYNC_INTERVAL = TimeRange.valueOf("25s-35s");

  /**
   * The default max number of times at which JNDI servers synchronize their state with others.
   *
   * @see Consts#JNDI_SYNC_MAX_COUNT
   */
  public static final int DEFAULT_JNDI_SYNC_MAX_COUNT = 5;

  /**
   * The time to for which threads should be kept alive in the spawning thread pool.
   *
   * @see Consts#SPAWN_THREADS_KEEP_ALIVE
   */
  public static final TimeValue DEFAULT_SPAWN_KEEP_ALIVE = TimeValue.createSeconds(30);

  /**
   * The number of core threads in the spawning thread pool.
   *
   * @see Consts#SPAWN_CORE_THREADS
   *
   */
  public static final int  DEFAULT_SPAWN_CORE_POOL_SIZE = 5;

  /**
   * The max number of threads in the spawning thread pool.
   *
   * @see Consts#SPAWN_MAX_THREADS
   */
  public static final int  DEFAULT_SPAWN_MAX_POOL_SIZE  = 10;

  /**
   * The queue size of the spawning thread pool.
   *
   * @see Consts#SPAWN_THREADS_QUEUE_SIZE
   */
  public static final int  DEFAULT_SPAWN_QUEUE_SIZE = 100;
  

  private Defaults() {
  }
}
