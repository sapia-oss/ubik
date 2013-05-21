package org.sapia.ubik.mcast.tcp.netty;

/**
 * Constants specific to the {@link NettyTransportProvider}.
 * 
 * @author yduchesne
 *
 */
public interface NettyMcastConsts {

  /**
   * Corresponds to the <code>ubik.rmi.transport.mcast.netty.server.io.core-threads</code> property, used to specify
   * the number of Netty core threads for the IO processing thread pool on the server side (defaults to 1).
   */
  public static final String SERVER_IO_CORE_THREADS_KEY = "ubik.rmi.transport.mcast.netty.server.io.core-threads";
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.mcast.netty.server.io.max-threads</code> property, used to specify
   * the number of Netty max threads for the IO processing thread pool on the server side (defaults to 5).
   */
  public static final String SERVER_IO_MAX_THREADS_KEY = "ubik.rmi.transport.mcast.netty.server.io.max-threads";  
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.mcast.netty.server.io.queue-size</code> property, used to specify
   * the size of the blocking queue of the IO processing thread pool on the server side (defaults to 50).
   */
  public static final String SERVER_IO_QUEUE_SIZE_KEY = "ubik.rmi.transport.mcast.netty.server.io.queue-size";
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.mcast.netty.server.io.keep-alive</code> property, used to specify
   * the keep-alive value (in seconds) of the IO processing thread pool on the server side (defaults to 30 seconds).
   */
  public static final String SERVER_IO_KEEP_ALIVE_KEY = "ubik.rmi.transport.mcast.netty.server.io.keep-alive";    
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.mcast.netty.server.worker.max-threads</code> property, used to specify
   * the number of Netty core threads for the worker thread pool on the server side (defaults to 5).
   */
  public static final String SERVER_WORKER_CORE_THREADS_KEY = "ubik.rmi.transport.mcast.netty.server.worker.core-threads";
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.mcast.netty.server.worker.max-threads</code> property, used to specify
   * the number of Netty max threads for the worker thread pool on the server side (defaults to 10).
   */
  public static final String SERVER_WORKER_MAX_THREADS_KEY = "ubik.rmi.transport.mcast.netty.server.worker.max-threads";  

  /**
   * Corresponds to the <code>ubik.rmi.transport.mcast.netty.server.worker.queue-size</code> property, used to specify
   * the number size of the blocking queue of the "worker" thread pool on the client side (defaults to 100).
   */
  public static final String SERVER_WORKER_QUEUE_SIZE_KEY = "ubik.rmi.transport.mcast.netty.server.worker.queue-size";  
  
  /**
   * Corresponds to the <code>ubik.rmi.transport.mcast.netty.server.worker.keep-alive</code> property, used to specify
   * the keep-alive value (in seconds) of the "worker" thread pool on the client side (defaults to 30 seconds).
   */
  public static final String SERVER_WORKER_KEEP_ALIVE_KEY = "ubik.rmi.transport.mcast.netty.server.worker.keep-alive";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.mcast.netty.server.bind-address</code> system property.
   */
  public static final String SERVER_BIND_ADDRESS_KEY = "ubik.rmi.transport.mcast.netty.server.bind-address";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.mcast.netty.server.port</code> system property.
   */
  public static final String SERVER_PORT_KEY = "ubik.rmi.transport.mcast.netty.server.port";  

  /**
   * The default for {@link #CLIENT_IO_CORE_THREADS_KEY}. 
   */
  public static final int DEFAULT_SERVER_IO_CORE_THREADS = 1;
  
  /**
   * The default for {@link #CLIENT_IO_MAX_THREADS_KEY}. 
   */  
  public static final int DEFAULT_SERVER_IO_MAX_THREADS = 5;
  
  /**
   * The default for {@link #SERVER_IO_QUEUE_SIZE_KEY}. 
   */  
  public static final int DEFAULT_SERVER_IO_QUEUE_SIZE = 50;

  /**
   * The default for {@link #SERVER_IO_KEEP_VALIVE_KEY}. 
   */  
  public static final int DEFAULT_SERVER_IO_KEEP_ALIVE = 30;  
  
  /**
   * The default for {@link #SERVER_WORKER_CORE_THREADS_KEY}. 
   */
  public static final int DEFAULT_SERVER_WORKER_CORE_THREADS = 5;
  
  /**
   * The default for {@link #SERVER_WORKER_MAX_THREADS_KEY}. 
   */  
  public static final int DEFAULT_SERVER_WORKER_MAX_THREADS = 10;

  /**
   * The default for {@link #SERVER_WORKER_QUEUE_SIZE_KEY}. 
   */  
  public static final int DEFAULT_SERVER_WORKER_QUEUE_SIZE = 100;
  
  /**
   * The default for {@link #SERVER_WORKER_KEEP_VALIVE_KEY}. 
   */  
  public static final int DEFAULT_SERVER_WORKER_KEEP_ALIVE = 30;  

}

