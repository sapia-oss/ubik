package org.sapia.ubik.rmi;

import java.util.concurrent.TimeUnit;

import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.util.TimeRange;
import org.sapia.ubik.util.TimeValue;

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
   * The default multicast address.
   */
  public static final String DEFAULT_MCAST_ADDR = "231.173.5.7";

  /**
   * The default multicast port.
   */
  public static final int DEFAULT_MCAST_PORT = 5454;

  /**
   * The default domain.
   */
  public static final String DEFAULT_DOMAIN = "default";

  /**
   * The default TCP port range, for selecting random ports.
   */
  public static final String DEFAULT_TCP_PORT_RANGE = "[1025 - 32000]";
  
  /**
   * The default client socket connection timeout.
   */
  public static final int DEFAULT_CLIENT_CONNECTION_TIMEOUT = 50;

  /**
   * The default client socket connection max retry.
   */
  public static final int DEFAULT_CLIENT_CONNECTION_MAX_RETRY = 3;

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
   * The default marshalling buffer size (see {@link Consts#MARSHALLING_BUFSIZE}).
   */
  public static final int DEFAULT_MARSHALLING_BUFSIZE = 512;
  
  /**
   * The default TTL for UDP multicast packets (see {@link Consts#MCAST_TTL}).
   */
  public static final int DEFAULT_TTL = 32;

  /**
   * The default synchronous response timeout (see
   * {@link Consts#MCAST_SYNC_RESPONSE_TIMEOUT}).
   */
  public static final TimeValue DEFAULT_SYNC_RESPONSE_TIMEOUT = TimeValue.createMillis(10000);
  
  /**
   * The default synchronous response timeout (see
   * {@link Consts#MCAST_ASYNC_ACK_TIMEOUT}).
   */
  public static final TimeValue DEFAULT_ASYNC_ACK_TIMEOUT = TimeValue.createMillis(500);
  
  /**
   * The default healtcheck delegate timeout (see {@link Consts#MCAST_HEALTHCHECK_DELEGATE_TIMEOUT}).
   */
  public static final TimeValue DEFAULT_HEALTCHCHECK_DELEGATE_TIMEOUT = TimeValue.createMillis(200);

  /**
   * The default healtcheck delegate node count (see {@link Consts#MCAST_HEALTHCHECK_DELEGATE_COUNT}).
   */
  public static final int DEFAULT_HEALTCHCHECK_DELEGATE_COUNT = 2;
  
  /**
   * The default heartbeat response delay (see {@link Consts#MCAST_HEARTBEAT_RESPONSE_DELAY}).
   */
  public static final TimeRange DEFAULT_HEARTBEAT_RESPONSE_DELAY = TimeRange.valueOf("2000ms:3000ms");

  /**
   * The default heartbeat interval (see {@link Consts#MCAST_HEARTBEAT_TIMEOUT}).
   */
  public static final TimeValue DEFAULT_HEARTBEAT_TIMEOUT = TimeValue.createMillis(60000);
  
  /**
   * The default gossip interval (see {@link Consts#MCAST_HEARTBEAT_INTERVAL}).
   */  
  public static final TimeValue DEFAULT_GOSSIP_INTERVAL   = TimeValue.createMillis(1000);

  /**
   * The default gossip interval (see {@link Consts#MCAST_GOSSIP_NODE_COUNT}).
   */  
  public static final int DEFAULT_GOSSIP_NODE_COUNT = 3;

  /**
   * The default interval at which the event channel thread should run (see {@link Consts#MCAST_CONTROL_THREAD_INTERVAL}).
   */
  public static final TimeValue DEFAULT_CONTROL_THREAD_INTERVAL = TimeValue.createMillis(1000);
  
  /**
   * The size for the splits of control notifications.
   */
  public static final int DEFAULT_CONTROL_SPLIT_SIZE = 5;
  
  /**
   * The default number of maximum client connections for each remote peers.
   *
   * @see Consts#MCAST_MAX_CLIENT_CONNECTIONS
   */
  public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 3;

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
   * The default broadcast monitor interval.
   * 
   * @see Consts#MCAST_BROADCAST_MONITOR_INTERVAL
   */
  public static final TimeValue DEFAULT_BROADCAST_MONITOR_INTERVAL = TimeValue.createMillis(5000);
  
  /**
   * The default auto-broadcast interval.
   * 
   * @see Consts#MCAST_AUTO_BROADCAST_INTERVAL
   */
  public static final TimeValue DEFAULT_AUTO_BROADCAST_INTERVAL = TimeValue.createMillis(15000);
  
  /**
   * The default auto-broadcast threshold.
   * 
   * @see Consts#MCAST_AUTO_BROADCAST_THRESHOLD
   */
  public static final int DEFAULT_AUTO_BROADCAST_THRESHOLD = 0;

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
   * The default core pool size of the worker thread pool.
   * 
   * @see Consts#SERVER_CORE_THREADS
   */
  public static final int DEFAULT_WORKER_CORE_POOL_SIZE   = 25;
  
  /**
   * The default max pool size of the worker thread pool.
   * 
   * @see Consts#SERVER_MAX_THREADS
   */
  public static final int DEFAULT_WORKER_MAX_POOL_SIZE    = 40;

  /**
   * The default queue size of the worker thread pool.
   * 
   * @see Consts#SERVER_THREADS_QUEUE_SIZE
   */
  public static final int DEFAULT_WORKER_QUEUE_SIZE       = 100;
  
  /**
   * The default idle time assigned to the worker thread pool.
   * 
   * @see Consts#SERVER_THREADS_KEEP_ALIVE
   */
  public static final TimeValue DEFAULT_WORKER_KEEP_ALIVE = new TimeValue(30, TimeUnit.SECONDS);
  
  /**
   * The default core pool size of the worker thread pool.
   * 
   * @see Consts#SERVER_OUTBOUND_CORE_THREADS
   */
  public static final int DEFAULT_OUTBOUND_CORE_POOL_SIZE   = 10;
  
  /**
   * The default max pool size of the worker thread pool.
   * 
   * @see Consts#SERVER_OUTBOUND_MAX_THREADS
   */
  public static final int DEFAULT_OUTBOUND_MAX_POOL_SIZE    = 25;

  /**
   * The default queue size of the worker thread pool.
   * 
   * @see Consts#SERVER_OUTBOUND_QUEUE_SIZE
   */
  public static final int DEFAULT_OUTBOUND_QUEUE_SIZE       = 1000;
  
  /**
   * The default idle time assigned to the worker thread pool.
   * 
   * @see Consts#SERVER_OUTBOUND_THREADS_KEEP_ALIVE
   */
  public static final TimeValue DEFAULT_OUTBOUND_KEEP_ALIVE = new TimeValue(30, TimeUnit.SECONDS);
  
  /**
   * The default number of threads assigned to the NIO selector pool.
   */
  public static final int DEFAULT_INBOUND_THREADS = Runtime.getRuntime().availableProcessors();
  
  /**
   * The default number of threads assigned to the NIO selection.
   */
  public static final int DEFAULT_UNICAST_INBOUND_THREADS = 1;
  
  /**
   * The default connection timeout for HTTP clients.
   */
  public static final int DEFAULT_HTTP_CLIENT_CONNECTION_TIMEOUT = 15000;
  
  /**
   * The default read timeout for HTTP clients.
   */ 
  public static final int DEFAULT_HTTP_CLIENT_READ_TIMEOUT = 5000;
  
  private Defaults() {
  }
}
