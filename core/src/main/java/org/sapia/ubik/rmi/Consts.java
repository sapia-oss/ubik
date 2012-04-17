package org.sapia.ubik.rmi;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.rmi.server.transport.TransportProvider;

/**
 * This class conveniently holds constants that correspond to the
 * system properties that can be define to influence Ubik RMI's runtime
 * behavior.
 *
 * @author Yanick Duchesne
 */
public interface Consts {
  
  /**
   * This constant corresponds to the property that identifies ubik's JNDI domain.
   * (the property is <code>ubik.jndi.domain</code>).
   */  
  public static final String UBIK_DOMAIN_NAME = "ubik.jndi.domain";    
  
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
  * This constant corresponds to the 'ubik.rmi.address-pattern' property key. 
  * The property should be used to specify a regular expression based upon which the
  * address of this host is chosen (if this host has more than one network interfaces).
  *
  * @see org.sapia.ubik.util.Localhost
  */
  public static final String IP_PATTERN_KEY = "ubik.rmi.address-pattern";

  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.port' property key. It
   * is used to bind a multicast port value in a properties/map instance.
   */
  public static final String MCAST_PORT_KEY = "ubik.rmi.naming.mcast.port";

  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.address' property key. It
   * is used to bind a multicast address value in a properties/map instance.
   */
  public static final String MCAST_ADDR_KEY = "ubik.rmi.naming.mcast.address";
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.bufsize' property key. It
   * is used to set the size of buffers that handle UDP datagrams. A too small value
   * may result in multicast events not being sent/received. Defaults to 3072 bytes.
   */
  public static final String MCAST_BUFSIZE_KEY = "ubik.rmi.naming.mcast.bufsize";  
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.sender.count' property key. It
   * is used to set the number of sender threads that may be used in {@link UnicastDispatcher} or {@link BroadcastDispatcher}
   * implementations.
   */
  public static final String MCAST_SENDER_COUNT = "ubik.rmi.naming.mcast.sender.count";
  
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.handler.count' property key. It
   * is used to set the number of worker threads that handler request in {@link UnicastDispatcher} 
   * or {@link BroadcastDispatcher} implementations.
   */  
  public static final String MCAST_HANDLER_COUNT = "ubik.rmi.naming.mcast.handler.count";
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.response.timeout' property key. The value
   * is expected to indicate the timeout (in millis) when waiting for synchronous responses.
   */
  public static final String MCAST_SYNC_RESPONSE_TIMEOUT = "ubik.rmi.naming.mcast.response.timeout";
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.heartbeat.timeout' property key. It
   * is used to determine the interval (in millis) after which nodes that haven't sent a heartbeat
   * are considered down (defaults to 90000).
   */
  public static final String MCAST_HEARTBEAT_TIMEOUT = "ubik.rmi.naming.mcast.heartbeat.timeout";

  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.heartbeat.interval' property key. It
   * is used to determine the interval (in millis) at which nodes send their heartbeat to the other nodes
   * (defaults to 60000).
   * <p>
   * The value of this property should consistent with the value given to the heartbeat timeout: it
   * should not be more.
   */
  public static final String MCAST_HEARTBEAT_INTERVAL = "ubik.rmi.naming.mcast.heartbeat.interval";      
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.control.response.timeout' property key. It
   * is used to determine the amount of time (in millis) that is allowed for receiving control responses
   * (defaults to 60000).
   */
  public static final String MCAST_CONTROL_RESPONSE_TIMEOUT = "ubik.rmi.naming.mcast.control.response.timeout";
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.control.batch.size' property key. It is used 
   * to specify the size of the batches of control notifications and requests (defaults to 50).
   */
  public static final String MCAST_CONTROL_BATCH_SIZE = "ubik.rmi.naming.mcast.control.batch.size";

  /**
   * Identifies the unicast provider to use as part of {@link EventChannel}s.
   */
  public static final String UNICAST_PROVIDER  = "ubik.rmi.naming.unicast.provider";

  /**
   * Identifies the UPD unicast provider.
   */
  public static final String UNICAST_PROVIDER_UDP = "ubik.rmi.naming.unicast.udp";
  
  /**
   * Identifies the TCP unicast provider.
   */
  public static final String UNICAST_PROVIDER_TCP = "ubik.rmi.naming.unicast.tcp";
  
  /**
   * Identifies the in-memory unicast provider.
   */
  public static final String UNICAST_PROVIDER_MEMORY = "ubik.rmi.naming.unicast.memory";  
  
  /**
   * Identifies the broadcast provider to use as part of {@link EventChannel}s.
   */
  public static final String BROADCAST_PROVIDER  = "ubik.rmi.naming.broadcast.provider";

  /**
   * Identifies the UPD broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_UDP = "ubik.rmi.naming.broadcast.udp";
  
  /**
   * Identifies the Avis broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_AVIS = "ubik.rmi.naming.broadcast.avis";
  
  /**
   * Identifies the Avis URL.
   */
  public static final String BROADCAST_AVIS_URL = "ubik.rmi.naming.broadcast.avis.url";
  
  /**
   * Identifies the in-memory broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_MEMORY = "ubik.rmi.naming.broadcast.mem";
  
  /**
   * Identifies the node of the broadcast memory address.
   */
  public static final String BROADCAST_MEMORY_NODE = "ubik.rmi.naming.broadcast.memory.node";  
  
  /**
   * This constant corresponds to the <code>ubik.rmi.marshalling</code> property key. If the property's
   * value is true, then the Ubik RMI runtime will wrap remote method invocation
   * parameters in org.sapia.ubik.rmi.transport.MarshalledObject instances prior sending
   * the parameters over the wire.
   */
  public static final String MARSHALLING = "ubik.rmi.marshalling";  

  /**
   * This constant corresponds to the <code>ubik.rmi.marshalling.buffer.size</code> property key.
   * It indicates the buffer size (in bytes) to use when performing marshalling/unmarshalling. Defaults
   * to 512.
   */
  public static final String MARSHALLING_BUFSIZE = "ubik.rmi.marshalling.buffer.size";  

  /**
   * The default marshalling buffer size (see {@link #MARSHALLING_BUFSIZE}).
   */
  public static final int DEFAULT_MARSHALLING_BUFSIZE = 512;
  
  /**
   * Defines the logging verbosity; must be one of the following:
   * debug, info, warning, error - system property name: <code>ubik.rmi.log.level</code>.
   * Defaults to "error".
   */
  public static final String LOG_LEVEL = "ubik.rmi.log.level";

  /**
   * Specifies if call-back should be used (true) or not  (false) - system property name:
   * <code>ubik.rmi.callback.enabled</code>. Defaults to "false".
   */
  public static final String CALLBACK_ENABLED = "ubik.rmi.callback.enabled";
  
  /**
   * Interval (in millis) at which the server-side distributed garbage collector wakes up -
   * system property name: <code>ubik.rmi.server.gc.interval</code>. Defaults to 10 secs.
   */
  public static final String SERVER_GC_INTERVAL = "ubik.rmi.server.gc.interval";

  /**
   * Delay (in millis) after which clients that have not performed a "ping" are considered down -
   * system property name: <code>ubik.rmi.server.gc.timeout</code>. Defaults to 30 secs.
   */
  public static final String SERVER_GC_TIMEOUT = "ubik.rmi.server.gc.timeout";

  /**
   * Specifies the maximum number of processing server threads - system property name:
   * <code>ubik.rmi.server.max-threads</code>. No maximum is defined by default.
   */
  public static final String SERVER_MAX_THREADS = "ubik.rmi.server.max-threads";

  /**
   * Specifies the maximum number of threads that process method invocation callbacks
   * - system property name: code>ubik.rmi.server.callback.max-threads</code>. Defaults to 5.
   */
  public static final String SERVER_CALLBACK_MAX_THREADS = "ubik.rmi.server.callback.max-threads";
  
  /**
   * Specifies the maximum number of threads that process method invocation callback
   * responses waiting on the outgoing queue.
   * - system property name: code>ubik.rmi.server.callback.outqueue.max-threads</code>. Defaults to 2.
   */
  public static final String SERVER_CALLBACK_OUTQUEUE_THREADS = "ubik.rmi.server.callback.outqueue.threads";  
  
  /**
   * This constant corresponds to the <code>ubik.rmi.server.reset-interval</code>
   * system property, which defines at which interval (in millis) the 
   * {@link ObjectOutputStream} and {@link ObjectInputStream} resets occur.
   * System property: <code>ubik.rmi.server.reset-interval</code>.
   */
  public static final String SERVER_RESET_INTERVAL = "ubik.rmi.server.reset-interval";  

  /**
   * Interval (in millis) at which the client distributed garbage collector wakes up -
   * system property name: <code>ubik.rmi.client.gc.interval</code>. Defaults to 10 seconds.
   */
  public static final String CLIENT_GC_INTERVAL = "ubik.rmi.client.gc.interval";
  
  /**
   * Specifies the size of the batch of OIDs that will be sent to remote servers by the client GC.
   * The latter tracks the OIDs whose corresponding remote reference are null (when means that they
   * have locally been dereferenced) - system property: <code>ubik.rmi.client.gc.batch.size</code>.
   */
  public static final String CLIENT_GC_BATCHSIZE = "ubik.rmi.client.gc.batch.size";
  
  /**
   * Specifies the number of remote references at which clients will start invoking the JVM's garbage
   * collector explicitely (a number equal to or lower than 0 means that no threshold is to be taken
   * into account - this property will then have no effect). This property is used to force client GC's
   * to run regularly, so that unreachable remote references on the client side are dereferenced on
   * the server side. System property: <code>ubik.rmi.client.gc.threshold</code>.
   */
  public static final String CLIENT_GC_THRESHOLD = "ubik.rmi.client.gc.threshold";  
  
  /**
   * Specifies the timeout (in millis) of client callbacks (the delay after which the
   * latter tracks the OIDs whose corresponding remote reference are null (which means
   * that they have locally been dereferenced). The client GC indirectly interacts with the VM's
   * GC. All OIDs whose corresponding remote reference has been locally GC'ed are sent to the
   * originating server (in batches whose size corresponds to the property defined by this constant).
   */
  public static final String CLIENT_CALLBACK_TIMEOUT = "ubik.rmi.client.callback.timeout";
  
  /**
   * Specifies if colocated calls should be supported or not (defaults to <code>true</code>).
   * System property: System property: <code>ubik.rmi.colocated.calls.enabled</code>.
   */
  public static final String ENABLE_COLOCATED_CALLS  = "ubik.rmi.colocated.calls.enabled";
  
  /**
   * This constant corresponds to the system property that defines the load factor
   * of the hashmap used by the <code>ObjectTable</code> to keep remote objects 
   * - system property: <code>ubik.rmi.object-table.load-factor</code>.
   */
  public static final String OBJECT_TABLE_LOAD_FACTOR = "ubik.rmi.object-table.load-factor";
  
  /**
   * This constant corresponds to the system property that defines the initial capacity
   * of the hashmap used by the <code>ObjectTable</code> to keep remote objects 
   * - system property: <code>ubik.rmi.object-table.initial-capacity</code>.
   */
  public static final String OBJECT_TABLE_INITCAPACITY = "ubik.rmi.object-table.initial-capacity";  

  /**
   * Specifies the "transport type" to use. Property name: <code>ubik.rmi.transport.type</code>
   *
   * @see org.sapia.ubik.rmi.server.Hub#exportObject(Object, java.util.Properties)
   */
  public static final String TRANSPORT_TYPE = "ubik.rmi.transport.type";
  
  /**
   * Determines if statistics should be turned on - system property: <code>ubik.rmi.stats.enabled</code>. 
   * Value must be <code>true</code> or <code>false</code> (if not set, same effect as <code>false</code>).
   */
  public static final String STATS_ENABLED = "ubik.rmi.stats.enabled";
  
  /**
   * Determines the interval at which Ubik stats must be dumped - system property: <code>ubik.rmi.stats.dump.interval</code>. Note:
   * this property will only be taken into account if stats are enabled - see {@link #STATS_ENABLED}. 
   * Value must be in seconds. If it is set to 0 or less, or not set, no dump will occur. Otherwise, stats dump will
   * be done to stdout.
   */
  public static final String STATS_DUMP_INTERVAL = "ubik.rmi.stats.dump.interval";  
  
  /**
   * Determines if Ubik's JMX beans should be registered with the platform's MBeanServer - system property: <code>ubik.rmi.stats.enabled</code>. 
   * Value must be <code>true</code> or <code>false</code> (if not set, same effect as <code>false</code>).
   */
  public static final String JMX_ENABLED = "ubik.rmi.stats.enabled";    

  /**
   * This constant corresponds to the system property that prefixes the configured
   * {@link TransportProvider}s to plug into the {@link TransportManager}.
   * When it initializes, the latter indeed looks for all system properties starting
   * with the <code>ubik.rmi.transport.provider</code> prefix. This prefix must be suffixed
   * with an arbitrary value - so that multiple provider definitions do not overwrite each other.
   * The property's value is the name of the transport provider's class. For example, given
   * the <code>org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider</code> class,
   * the property could be: <code>ubik.rmi.transport.provider.socket</code>; the associated value
   * would be the above-mentioned class name.
   * <p>
   * At initialization, the {@link TransportManager} will dynamically instantiate all
   * providers that have been thus defined and register them internally.
   *
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   * @see org.sapia.ubik.rmi.server.transport.TransportManager
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider
   * @see org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider
   */
  public static final String TRANSPORT_PROVIDER = "ubik.rmi.transport.provider";
  
  /**
   * This constant corresponds to the system property (<code>ubik.rmi.transport.serialization.provider</code>)
   * that specifies which serialization provider should be used: <code>jboss</code> or <code>jdk</code>.
   * <p>
   * By default, unless the <code>jdk</code> provider is specified or the JBoss serialization implementation 
   * cannot be found in the classpath, the JBoss implementation will be used.
   * 
   */
  public static final String SERIALIZATION_PROVIDER = "ubik.rmi.transport.serialization.provider";
 
  /**
   * Corresponds to the property value that should be used to specify the JBoss serialization provider.
   */
  public static final String SERIALIZATION_PROVIDER_JBOSS = "jboss";
  
  /**
   * Corresponds to the property value that should be used to specify the JDK serialization provider.
   */
  public static final String SERIALIZATION_PROVIDER_JDK   = "jdk";

}
