package org.sapia.ubik.rmi;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.sapia.ubik.log.LogOutput;
import org.sapia.ubik.mcast.BroadcastDispatcher;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.server.transport.TransportProvider;

/**
 * This class conveniently holds constants that correspond to the system
 * properties that can be define to influence Ubik RMI's runtime behavior.
 *
 * @author Yanick Duchesne
 */
public interface Consts {

  /**
   * This constant corresponds to the property that identifies ubik's JNDI
   * domain. (the property is <code>ubik.jndi.domain</code>).
   */
  public static final String UBIK_DOMAIN_NAME = "ubik.jndi.domain";

  /**
   * Defines the logging verbosity; must be one of the following: debug, info,
   * warning, error - system property name: <code>ubik.rmi.log.level</code>.
   * Defaults to "error".
   */
  public static final String LOG_LEVEL = "ubik.rmi.log.level";

  /**
   * Defines the {@link LogOutput} to use.
   */
  public static final String LOG_OUTPUT_CLASS = "ubik.rmi.log.output.class";

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
  public static final int DEFAULT_CLIENT_CONNECTION_TIMEOUT = 2000;
  
  /**
   * This constant corresponds to the <code>ubik.rmi.address-pattern</code>
   * property. The property should be used to specify a regular expression
   * based upon which the address of this host is chosen (if this host has more
   * than one network interfaces).
   *
   * @see org.sapia.ubik.util.Localhost
   */
  public static final String IP_PATTERN_KEY = "ubik.rmi.address-pattern";

  
  /**
   * This constant corresponds to the <code>ubik.rmi.tcp.port-range</code>
   * property. The property should be used to specify a port range literal of the form <code>[min_port - max_port]</code>.
   * Random ports will be selected within that range (the default is <code>[1025 - 32000]</code>).
   */
  public static final String TCP_PORT_RANGE = "ubik.rmi.tcp.port-range";
  
  /**
   * This constant corresponds to the <code>ubik.rmi.naming.sync.lookup.batch-size</code>
   * property. It is used to bind a multicast port value in a properties/map
   * instance.
   */
  public static final String JNDI_SYNC_LOOKUP_BATCH_SIZE = "ubik.rmi.naming.sync.lookup.batch-size";

  /**
   * The number of milliseconds to wait for on the client-side when attempting to discover a JNDI server (defaults to 10000).
   */
  public static final String JNDI_CLIENT_DISCO_TIMEOUT = "ubik.rmi.naming.client.disco.timeout";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.lazy.lookup.interval</code>
   * property. It is used to specify the interval (in millis) at which lazy stub invocation handlers
   * should perform lookups.
   */
  public static final String JNDI_LAZY_LOOKUP_INTERVAL = "ubik.rmi.naming.lazy.lookup.interval";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.sync.interval</code>
   * property. It is used to specify the time range that is used to calculate the interval at which
   * JNDI servers should synchronize their state with the other JNDI servers in the cluster (defaults to 25:30 secs).
   */
  public static final String JNDI_SYNC_INTERVAL = "ubik.rmi.naming.sync.interval";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.sync.max-count</code>
   * property. It is used to specify the maximum number of times JNDI servers should
   * synchronize their state with the other JNDI servers in the cluster (defaults to 5 times,
   * a negative value is interpreted as no maximum).
   */
  public static final String JNDI_SYNC_MAX_COUNT = "ubik.rmi.naming.sync.max-count";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.mcast.port</code>
   * property. It is used to bind a multicast port value in a properties/map
   * instance.
   */
  public static final String MCAST_PORT_KEY = "ubik.rmi.naming.mcast.port";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.mcast.address</code>
   * property. It is used to bind a multicast address value in a
   * properties/map instance.
   */
  public static final String MCAST_ADDR_KEY = "ubik.rmi.naming.mcast.address";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.mcast.ttl</code>
   * property. It is used to specify the time-to-live of UDP multicast
   * packets.
   */
  public static final String MCAST_TTL = "ubik.rmi.naming.mcast.ttl";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.mcast.bufsize</code>
   * property. It is used to set the size of buffers that handle UDP
   * datagrams. A too small value may result in multicast events not being
   * sent/received. Defaults to 3072 bytes.
   */
  public static final String MCAST_BUFSIZE_KEY = "ubik.rmi.naming.mcast.bufsize";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.sender.count</code> property. It is used to
   * set the number of sender threads that may be used in
   * {@link UnicastDispatcher} or {@link BroadcastDispatcher} implementations.
   */
  public static final String MCAST_SENDER_COUNT = "ubik.rmi.naming.mcast.sender.count";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.handler.count</code> property. It is used
   * to set the number of worker threads that handler request in
   * {@link UnicastDispatcher} or {@link BroadcastDispatcher} implementations.
   */
  public static final String MCAST_HANDLER_COUNT = "ubik.rmi.naming.mcast.handler.count";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.handler.queue.size</code> property. It is used
   * to set the size of the queue that buffers incoming requests in
   * {@link UnicastDispatcher} or {@link BroadcastDispatcher} implementations.
   */
  public static final String MCAST_HANDLER_QUEUE_SIZE = "ubik.rmi.naming.mcast.handler.queue.size";
 
  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.async.ack.timeout</code> property. The value
   * is expected to indicate the timeout (in millis) when waiting for
   * asynchronous response acks upon dispatching remote events.
   */
  public static final String MCAST_ASYNC_ACK_TIMEOUT = "ubik.rmi.naming.mcast.async.ack.timeout";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.heartbeat.timeout</code> property. It is
   * used to determine the interval (in millis) after which nodes that haven't
   * sent a heartbeat are considered down (defaults to 90000).
   */
  public static final String MCAST_HEARTBEAT_TIMEOUT = "ubik.rmi.naming.mcast.heartbeat.timeout";
  
  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.healthcheck.delegate.timeout</code> property. It is
   * used to determine amount of time to wait for (in millis) on synchronous health
   * check requests (defaults to 200).
   */
  public static final String MCAST_HEALTHCHECK_DELEGATE_TIMEOUT = "ubik.rmi.naming.mcast.healthcheck.delegate.timeout";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.control.thread.interval</code> property. It is
   * used to configure the interval in between each run of the event channel's main thread (defaults to 1000).
   * <p>
   * The value of this property must be less than or equal to {@link #MCAST_GOSSIP_INTERVAL} in order to make sense.
   */
  public static final String MCAST_CONTROL_THREAD_INTERVAL = "ubik.rmi.naming.mcast.control.thread.interval";
  
  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.healthcheck.delegate.count</code> property. It indicates
   * the number of delegate nodes to use when performing health check (defaults to 2).
   */
  public static final String MCAST_HEALTHCHECK_DELEGATE_COUNT = "ubik.rmi.naming.mcast.healthcheck.delegate.count";
  
  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.gossip.interval</code> property. It is
   * used to determine the interval (in millis) at which nodes send their
   * gossip to the other nodes (defaults to 3000).
   * <p>
   * The value of this property should consistent with the value given to the
   * gossip timeout: it should not be more.
   */
  public static final String MCAST_GOSSIP_INTERVAL = "ubik.rmi.naming.mcast.gossip.interval";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.gossip.enabled</code> property. 
   * Used for testing purposes (defaults to true). DO NOT USE OTHERWISE.
   */
  public static final String MCAST_GOSSIP_ENABLED = "ubik.rmi.naming.mcast.gossip.enabled";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.gossip.count</code> property. 
   * Indicates the number of random nodes at a time to gossip to.
   */
  public static final String MCAST_GOSSIP_NODE_COUNT = "ubik.rmi.naming.mcast.gossip.node-count";
  
  /**
   * This constant corresponds to the
   * <code>ubik.rmi.naming.mcast.control.split.size</code> property. It is
   * used to specify the size of the batches of control notifications and
   * requests (defaults to 5).
   */
  public static final String MCAST_CONTROL_SPLIT_SIZE = "ubik.rmi.naming.mcast.control.split.size";
  
  /**
   * Corresponds to the
   * <code>ubik.rmi.naming.mcast.tcp.client.max-connections</code> property.
   * The value of this property specifies the number of connections that should
   * be pooled on the client side for each remote peer (defaults to 3).
   */
  public static final String MCAST_MAX_CLIENT_CONNECTIONS = "ubik.rmi.naming.mcast.tcp.client.max-connections";

  /**
   * Corresponds to the
   * <code>ubik.rmi.naming.mcast.broadcast.monitor.interval</code> property.
   * The value of this property specifies the number of seconds between reconnection attempts when a connection failure occurs
   * on the broadcast dispatcher (defaults to 5000 millis).
   */
  public static final String MCAST_BROADCAST_MONITOR_INTERVAL = "ubik.rmi.naming.mcast.broadcast.monitor.interval";

  /**
   * Corresponds to the
   * <code>ubik.rmi.naming.mcast.channel.start.delay</code> property.
   * The value of this property specifies the time range to use for calculating the delay before the event channel publishes its
   * presence for the first time (defaults to 500:3000 millis).
   */
  public static final String MCAST_CHANNEL_START_DELAY = "ubik.rmi.naming.mcast.channel.start.delay";

  /**
   * Corresponds to the
   * <code>ubik.rmi.naming.mcast.channel.pub-interval</code> property.
   * The value of this property specifies the time range used for calculating the interval at which an event channel
   * publishes itself, either upon resync or in the context of master broadcast (defaults to 1000:5000 millis).
   */
  public static final String MCAST_CHANNEL_PUBLISH_INTERVAL = "ubik.rmi.naming.mcast.channel.pub-interval";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.mcast.channel.reuse</code>
   * property. It is used in test to indicate if {@link EventChannel} instance reuse should be enabled.
   * <p>
   * When testing event channel behavior in-memory, this property should be set to false.
   */
  public static final String MCAST_REUSE_EXISTINC_CHANNELS = "ubik.rmi.naming.mcast.channel.reuse";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.mcast.channel.auto-broadcast.enabled</code> flag: enables/disables auto-broadcast
   * (true by default).
   */
  public static final String MCAST_AUTO_BROADCAST_ENABLED = "ubik.rmi.naming.mcast.channel.auto-broadcast.enabled";
  
  /**
   * This constant corresponds to the <code>ubik.rmi.naming.mcast.channel.auto-broadcast.threshold</code>: determines the number of nodes
   * in the cluster below or equal to which auto-broadcast is activated (defaults to 0).
   */
  public static final String MCAST_AUTO_BROADCAST_THRESHOLD = "ubik.rmi.naming.mcast.channel.auto-broadcast.threshold";

  /**
   * This constant corresponds to the <code>ubik.rmi.naming.mcast.channel.auto-broadcast.interval</code>: determines
   * the interval at which auto-broadcast is run (defaults to 15000 millis).
   */
  public static final String MCAST_AUTO_BROADCAST_INTERVAL = "ubik.rmi.naming.mcast.channel.auto-broadcast.interval";
 
  /**
   * Identifies the unicast provider to use as part of {@link EventChannel}s.
   */
  public static final String UNICAST_PROVIDER = "ubik.rmi.naming.unicast.provider";

  /**
   * Identifies the UPD unicast provider.
   */
  public static final String UNICAST_PROVIDER_UDP = "udp";

  /**
   * Identifies the TCP/blocking I/O unicast provider.
   */
  public static final String UNICAST_PROVIDER_TCP_BIO = "tcp-bio";
  
  /**
   * Identifies the TCP/non-blocking I/O unicast provider (based on Mina).
   */
  public static final String UNICAST_PROVIDER_TCP_NIO = "tcp-nio";

  /**
   * Identifies the in-memory unicast provider.
   */
  public static final String UNICAST_PROVIDER_MEMORY = "memory";

  /**
   * Identifies the broadcast provider to use as part of {@link EventChannel}s.
   */
  public static final String BROADCAST_PROVIDER = "ubik.rmi.naming.broadcast.provider";

  /**
   * Identifies the UPD broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_UDP = "udp";

  /**
   * Identifies the Avis broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_AVIS = "avis";

  /**
   * Identifies the Avis URL.
   */
  public static final String BROADCAST_AVIS_URL = "ubik.rmi.naming.broadcast.avis.url";

  /**
   * Identifies the Camel broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_CAMEL = "camel";
  
  /**
   * Identifies the AMQP broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_AMQP = "amqp";
  
  /**
   * Identifies the Camel endpoint URI for broadcast messages.
   */
  public static final String BROADCAST_CAMEL_ENDPOINT_URI = "ubik.rmi.naming.broadcast.camel.endpoint.uri";

  /**
   * Identifies the Hazelcast broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_HAZELCAST = "hazelcast";

  /**
   * Identifies the Halzecast topic name.
   */
  public static final String BROADCAST_HAZELCAST_TOPIC = "ubik.rmi.naming.broadcast.hazelcast.topic";

  /**
   * Identifies the in-memory broadcast provider.
   */
  public static final String BROADCAST_PROVIDER_MEMORY = "memory";

  /**
   * Identifies the node of the broadcast memory address.
   */
  public static final String BROADCAST_MEMORY_NODE = "ubik.rmi.naming.broadcast.memory.node";
  
  /**
   * Identifies the broadcast over multicast provider.
   */
  public static final String BROADCAST_PROVIDER_UNICAST = "broacast-unicast";
  
  /**
   * Identifies the value of the delegate unicast address.
   */
  public static final String BROADCAST_UNICAST_ADDRESS = "ubik.rmi.naming.broadcast.unicast.address";

  /**
   * Identifies the group membership provider.
   */
  public static final String GROUP_MEMBERSHIP_PROVIDER = "ubik.rmi.naming.group.membership.provider";

  /**
   * Identifies the in-memory group membership provider.
   */
  public static final String GROUP_MEMBERSHIP_PROVIDER_MEMORY    = "memory";

  /**
   * Identifies the zookeeper group membership provider.
   */
  public static final String GROUP_MEMBERSHIP_PROVIDER_ZOOKEEPER = "zk";

  /**
   * This constant corresponds to the <code>ubik.rmi.marshalling</code> property
   * key. If the property's value is true, then the Ubik RMI runtime will wrap
   * remote method invocation parameters in
   * org.sapia.ubik.rmi.transport.MarshalledObject instances prior sending the
   * parameters over the wire.
   */
  public static final String MARSHALLING = "ubik.rmi.marshalling";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.marshalling.buffer.size</code> property. It indicates
   * the buffer size (in bytes) to use when performing
   * marshalling/unmarshalling. Defaults to 512.
   */
  public static final String MARSHALLING_BUFSIZE = "ubik.rmi.marshalling.buffer.size";

  /**
   * The default marshalling buffer size (see {@link #MARSHALLING_BUFSIZE}).
   */
  public static final int DEFAULT_MARSHALLING_BUFSIZE = 512;

  /**
   * Specifies if call-back should be used (true) or not (false) - system
   * property name: <code>ubik.rmi.callback.enabled</code>. Defaults to "false".
   */
  public static final String CALLBACK_ENABLED = "ubik.rmi.callback.enabled";

  /**
   * Interval (in millis) at which the server-side distributed garbage collector
   * wakes up - system property name: <code>ubik.rmi.server.gc.interval</code>.
   * Defaults to 10 secs.
   */
  public static final String SERVER_GC_INTERVAL = "ubik.rmi.server.gc.interval";

  /**
   * Delay (in millis) after which clients that have not performed a "ping" are
   * considered down - system property name:
   * <code>ubik.rmi.server.gc.timeout</code>. Defaults to 30 secs.
   */
  public static final String SERVER_GC_TIMEOUT = "ubik.rmi.server.gc.timeout";

  /**
   * Specifies the number of core processing server threads - system property
   * name: <code>ubik.rmi.server.core-threads</code>. Defaults to 5.
   */
  public static final String SERVER_CORE_THREADS = "ubik.rmi.server.core-threads";

  /**
   * Specifies the maximum number of processing server threads - system property
   * name: <code>ubik.rmi.server.max-threads</code>. Defaults to 25.
   */
  public static final String SERVER_MAX_THREADS = "ubik.rmi.server.max-threads";

  /**
   * Specifies the duration of the idle period for server threads (in seconds).
   * System property name: <code>ubik.rmi.server.threads.keep-alive</code>.
   * Defaults to 30 (seconds).
   */
  public static final String SERVER_THREADS_KEEP_ALIVE = "ubik.rmi.server.threads.keep-alive";

  /**
   * Specifies the size of task processing queue for worker threads:
   * <code>ubik.rmi.server.threads.queue-size</code>. Defaults to 50.
   *
   * @see #CORE_MAX_THREADS
   * @see #SERVER_MAX_THREADS
   */
  public static final String SERVER_THREADS_QUEUE_SIZE = "ubik.rmi.server.threads.queue-size";

  /**
   * Specifies the number of core spawned threads - system property
   * name: <code>ubik.rmi.spawn.core-threads</code>. Defaults to 5.
   */
  public static final String SPAWN_CORE_THREADS = "ubik.rmi.spawn.core-threads";

  /**
   * Specifies the maximum number of spawned threads - system property
   * name: <code>ubik.rmi.spawn.max-threads</code>. Defaults to 10.
   */
  public static final String SPAWN_MAX_THREADS = "ubik.rmi.spawn.max-threads";

  /**
   * Specifies the duration of the idle period for spawned threads (in seconds).
   * System property name: <code>ubik.rmi.spawn.threads.keep-alive</code>.
   * Defaults to 30 (seconds).
   */
  public static final String SPAWN_THREADS_KEEP_ALIVE = "ubik.rmi.spawn.threads.keep-alive";

  /**
   * Specifies the size of task processing queue for spawned threads:
   * <code>ubik.rmi.spawn.threads.queue-size</code>. Defaults to 100.
   */
  public static final String SPAWN_THREADS_QUEUE_SIZE = "ubik.rmi.spawn.threads.queue-size";

  /**
   * Specifies the maximum number of threads that process method invocation
   * callbacks - system property name:
   * code>ubik.rmi.server.callback.max-threads</code>. Defaults to 5.
   */
  public static final String SERVER_CALLBACK_MAX_THREADS = "ubik.rmi.server.callback.max-threads";

  /**
   * Specifies the maximum number of threads that process method invocation
   * callback responses waiting on the outgoing queue. - system property name:
   * code>ubik.rmi.server.callback.outqueue.max-threads</code>. Defaults to 2.
   */
  public static final String SERVER_CALLBACK_OUTQUEUE_THREADS = "ubik.rmi.server.callback.outqueue.threads";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.server.reset-interval</code> system property, which defines
   * at which interval (in millis) the {@link ObjectOutputStream} and
   * {@link ObjectInputStream} resets occur. System property:
   * <code>ubik.rmi.server.reset-interval</code>.
   */
  public static final String SERVER_RESET_INTERVAL = "ubik.rmi.server.reset-interval";

  /**
   * Interval (in millis) at which the client distributed garbage collector
   * wakes up - system property name: <code>ubik.rmi.client.gc.interval</code>.
   * Defaults to 10 seconds.
   */
  public static final String CLIENT_GC_INTERVAL = "ubik.rmi.client.gc.interval";

  /**
   * Specifies the size of the batch of OIDs that will be sent to remote servers
   * by the client GC. The latter tracks the OIDs whose corresponding remote
   * reference are null (when means that they have locally been dereferenced) -
   * system property: <code>ubik.rmi.client.gc.batch.size</code>.
   */
  public static final String CLIENT_GC_BATCHSIZE = "ubik.rmi.client.gc.batch.size";

  /**
   * Specifies the number of remote references at which clients will start
   * invoking the JVM's garbage collector explicitely (a number equal to or
   * lower than 0 means that no threshold is to be taken into account - this
   * property will then have no effect). This property is used to force client
   * GC's to run regularly, so that unreachable remote references on the client
   * side are dereferenced on the server side. System property:
   * <code>ubik.rmi.client.gc.threshold</code>.
   */
  public static final String CLIENT_GC_THRESHOLD = "ubik.rmi.client.gc.threshold";

  /**
   * Specifies the timeout (in millis) of client callbacks (the delay after
   * which the latter tracks the OIDs whose corresponding remote reference are
   * null (which means that they have locally been dereferenced). The client GC
   * indirectly interacts with the VM's GC. All OIDs whose corresponding remote
   * reference has been locally GC'ed are sent to the originating server (in
   * batches whose size corresponds to the property defined by this constant).
   */
  public static final String CLIENT_CALLBACK_TIMEOUT = "ubik.rmi.client.callback.timeout";

  /**
   * Specifies the client socket connection timeout.
   */
  public static final String CLIENT_CONNECTION_TIMEOUT = "ubik.rmi.client.connection.timeout";

  /**
   * Specifies if colocated calls should be supported or not (defaults to
   * <code>true</code>). System property: System property:
   * <code>ubik.rmi.colocated.calls.enabled</code>.
   */
  public static final String COLOCATED_CALLS_ENABLED = "ubik.rmi.colocated.calls.enabled";

  /**
   * This constant corresponds to the system property that defines the load
   * factor of the hashmap used by the <code>ObjectTable</code> to keep remote
   * objects - system property: <code>ubik.rmi.object-table.load-factor</code>.
   */
  public static final String OBJECT_TABLE_LOAD_FACTOR = "ubik.rmi.object-table.load-factor";

  /**
   * This constant corresponds to the system property that defines the initial
   * capacity of the hashmap used by the <code>ObjectTable</code> to keep remote
   * objects - system property:
   * <code>ubik.rmi.object-table.initial-capacity</code>.
   */
  public static final String OBJECT_TABLE_INITCAPACITY = "ubik.rmi.object-table.initial-capacity";

  /**
   * Specifies the "transport type" to use. Property name:
   * <code>ubik.rmi.transport.type</code>
   *
   * @see org.sapia.ubik.rmi.server.Hub#exportObject(Object,
   *      java.util.Properties)
   */
  public static final String TRANSPORT_TYPE = "ubik.rmi.transport.type";

  /**
   * Determines if statistics should be turned on - system property:
   * <code>ubik.rmi.stats.enabled</code>. Value must be <code>true</code> or
   * <code>false</code> (if not set, same effect as <code>false</code>).
   */
  public static final String STATS_ENABLED = "ubik.rmi.stats.enabled";

  /**
   * Determines the interval (in SECONDS) at which Ubik stats must be dumped - system
   * property: <code>ubik.rmi.stats.dump.interval</code>. Note: this property
   * will only be taken into account if stats are enabled - see
   * {@link #STATS_ENABLED}. Value is expected to be in seconds. If it is set to
   * 0 or less, or not set, no dump will occur. Otherwise, stats dump will be
   * done to the stats output file.
   */
  public static final String STATS_DUMP_INTERVAL = "ubik.rmi.stats.dump.interval";

  /**
   * Determines if Ubik's JMX beans should be registered with the platform's
   * MBeanServer - system property: <code>ubik.rmi.jmx.enabled</code>. Value
   * must be <code>true</code> or <code>false</code> (if not set, same effect as
   * <code>false</code>).
   */
  public static final String JMX_ENABLED = "ubik.rmi.jmx.enabled";

  /**
   * This constant corresponds to the system property that prefixes the
   * configured {@link TransportProvider}s to plug into the
   * {@link TransportManager}. When it initializes, the latter indeed looks for
   * all system properties starting with the
   * <code>ubik.rmi.transport.provider</code> prefix. This prefix must be
   * suffixed with an arbitrary value - so that multiple provider definitions do
   * not overwrite each other. The property's value is the name of the transport
   * provider's class. For example, given the
   * <code>org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider</code>
   * class, the property could be:
   * <code>ubik.rmi.transport.provider.socket</code>; the associated value would
   * be the above-mentioned class name.
   * <p>
   * At initialization, the {@link TransportManager} will dynamically
   * instantiate all providers that have been thus defined and register them
   * internally.
   *
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   * @see org.sapia.ubik.rmi.server.transport.TransportManager
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider
   * @see org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider
   */
  public static final String TRANSPORT_PROVIDER = "ubik.rmi.transport.provider";

  /**
   * This constant corresponds to the system property (
   * <code>ubik.rmi.transport.serialization.provider</code>) that specifies
   * which serialization provider should be used: <code>jboss</code> or
   * <code>jdk</code>.
   * <p>
   * By default, unless the <code>jdk</code> provider is specified or the JBoss
   * serialization implementation cannot be found in the classpath, the JBoss
   * implementation will be used.
   *
   */
  public static final String SERIALIZATION_PROVIDER = "ubik.rmi.transport.serialization.provider";

  /**
   * Corresponds to the property value that should be used to specify the JBoss
   * serialization provider.
   */
  public static final String SERIALIZATION_PROVIDER_JBOSS = "jboss";

  /**
   * Corresponds to the property value that should be used to specify the JDK
   * serialization provider.
   */
  public static final String SERIALIZATION_PROVIDER_JDK = "jdk";

}
