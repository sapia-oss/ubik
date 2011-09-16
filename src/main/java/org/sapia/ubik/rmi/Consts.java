package org.sapia.ubik.rmi;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * This class conveniently holds constants that correspond to the
 * system properties that can be define to influence Ubik RMI's runtime
 * behavior.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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
   * This constant corresponds to the 'ubik.rmi.naming.mcast.post' property key. It
   * is used to bind a multicast port value in a properties/map instance.
   */
  public static final String MCAST_PORT_KEY = "ubik.rmi.naming.mcast.post";

  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.address' property key. It
   * is used to bind a multicast address value in a properties/map instance.
   */
  public static final String MCAST_ADDR_KEY = "ubik.rmi.naming.mcast.address";
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.bufsize' property key. It
   * is used to set the size of buffers that handle UDP datagrams. A too small value
   * may result in multicast events not being sent/received. Defaults to 5kb.
   */
  public static final String MCAST_BUFSIZE_KEY = "ubik.rmi.naming.mcast.bufsize";  
  
  /**
   * This constant corresponds to the 'ubik.rmi.naming.mcast.heartbeat.timeout' property key. It
   * is used to determine the interval (in millis) after which nodes that haven't sent a heartbeat
   * are considered down (defaults to 30000).
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
   * This constant corresponds to the 'ubik.rmi.marshalling' property key. If the property's
   * value is true, then the Ubik RMI runtime will wrap remote method invocation
   * parameters in org.sapia.ubik.rmi.transport.MarshalledObject instances prior sending
   * the parameters over the wire.
   */
  public static final String MARSHALLING = "ubik.rmi.marshalling";  

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
   * Specifies if dynamic code download is allows in this VM (true) or not (false) 
   * - system property name: <code>ubik.rmi.code-download.enabled</code>. Defaults to "false".
   */
  public static final String ALLOW_CODE_DOWNLOAD = "ubik.rmi.code-download.enabled";  

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
   * This constant corresponds to the system property that defines the load factor
   * of the hashmap used by the <code>ObjectTable</code> to keep remote objects 
   * - system property: <code>ubik.rmi.object-table.load-factor</code>.
   * 
   */
  public static final String OBJECT_TABLE_LOAD_FACTOR = "ubik.rmi.object-table.load-factor";
  
  /**
   * This constant corresponds to the system property that defines the initial capacity
   * of the hashmap used by the <code>ObjectTable</code> to keep remote objects 
   * - system property: <code>ubik.rmi.object-table.initial-capacity</code>.
   * 
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
   * <code>TransportProvider</code>s to plug into the <code>TransportManager</code>.
   * When it initializes, the latter indeed looks for all system properties starting
   * with the <code>ubik.rmi.transport.provider</code> prefix. This prefix must be suffixed
   * with an arbitrary value - so that multiple provider definitions do not overwrite each other.
   * The property's value is the name of the transport provider's class. For example, given
   * the <code>org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider</code> class,
   * the property could be: <code>ubik.rmi.transport.provider.socket</code>; the associated value
   * would be the above-mentioned class name.
   * <p>
   * At initialization, the <code>TransportManager</code> will dynamically instantiate all
   * providers that have been thus defined and register them internally.
   *
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   * @see org.sapia.ubik.rmi.server.transport.TransportManager
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider
   * @see org.sapia.ubik.rmi.server.transport.socket.SocketTransportProvider
   */
  public static final String TRANSPORT_PROVIDER = "ubik.rmi.transport.provider";
  
}
