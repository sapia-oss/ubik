package org.sapia.ubik.rmi.server.transport.netty;

/**
 * Constants specific to the {@link NettyTransportProvider}.
 * 
 * @author yduchesne
 *
 */
public interface NettyConsts {

  /**
   * Corresponds to the <code>ubik.rmi.transport.nio.netty.server.io.core-threads</code> property, used to specify
   * the number of Netty core threads for the IO worker thread pool on the server side (defaults to 3).
   */
  public static final String SERVER_IO_CORE_THREADS_KEY = "ubik.rmi.transport.nio.netty.server.io.core-threads";
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.nio.netty.server.io.max-threads</code> property, used to specify
   * the number of Netty max threads for the IO worker thread pool on the server side (defaults to 8).
   */
  public static final String SERVER_IO_MAX_THREADS_KEY = "ubik.rmi.transport.nio.netty.server.io.max-threads";  
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.nio.netty.server.io.queue-size</code> property, used to specify
   * the number size of the blocking queue of the IO worker thread pool on the server side (defaults to 100).
   */
  public static final String SERVER_IO_QUEUE_SIZE_KEY = "ubik.rmi.transport.nio.netty.server.io.queue-size";
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.nio.netty.server.io.keep-alive</code> property, used to specify
   * the keep-alive value (in seconds) of the IO worker thread pool on the server side (defaults to 30 seconds).
   */
  public static final String SERVER_IO_KEEP_ALIVE_KEY = "ubik.rmi.transport.nio.netty.server.io.keep-alive";    
  
  /**
   * This constant corresponds to the <code>ubik.rmi.transport.nio.netty.server.bind-address</code> system property.
   */
  public static final String SERVER_BIND_ADDRESS_KEY = "ubik.rmi.transport.nio.netty.server.bind-address";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.nio.netty.server.port</code> system property.
   */
  public static final String SERVER_PORT_KEY = "ubik.rmi.transport.nio.netty.server.port";  

  /**
   * The default for {@link #SERVER_IO_CORE_THREADS_KEY}. 
   */
  public static final int DEFAULT_SERVER_IO_CORE_THREADS = 3;
  
  /**
   * The default for {@link #CLIENT_IO_MAX_THREADS_KEY}. 
   */  
  public static final int DEFAULT_SERVER_IO_MAX_THREADS = 8;
  
  /**
   * The default for {@link #SERVER_IO_QUEUE_SIZE_KEY}. 
   */  
  public static final int DEFAULT_SERVER_IO_QUEUE_SIZE = 100;

  /**
   * The default for {@link #SERVER_IO_KEEP_VALIVE_KEY}. 
   */  
  public static final int DEFAULT_SERVER_IO_KEEP_ALIVE = 30;  
  
}

