package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.net.UbikServerSocketFactory;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Props;
import org.sapia.ubik.util.Time;


/**
 * Implements the {@link TransportProvider} interface over the standard java {@link Socket} class.
 *
 * @see java.net.Socket
 *
 * @author Yanick Duchesne
 */
public class SocketTransportProvider implements TransportProvider {
  
  /**
   * Constant corresponding to this provider class' transport type.
   */
  public static final String SOCKET_TRANSPORT_TYPE   = "tcp/socket";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.max-threads</code>
   * system property.
   */
  public static final String MAX_THREADS      = "ubik.rmi.transport.socket.max-threads";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.client-factory</code>
   * system property (it identifies the {@link ConnectionFactory} to use to create client-side
   * connections).
   */
  public static final String CLIENT_FACTORY   = "ubik.rmi.transport.socket.client-factory";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.server-factory</code>
   * system property. It identifies the {@link UbikServerSocketFactory} implementation to use.
   *
   */
  public static final String SERVER_FACTORY   = "ubik.rmi.transport.socket.server-factory";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.bind-address</code>
   * system property.
   */
  public static final String BIND_ADDRESS     = "ubik.rmi.transport.socket.bind-address";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.port</code>
   * system property.
   */
  public static final String PORT             = "ubik.rmi.transport.socket.port";
  
  protected static final long DEFAULT_RESET_INTERVAL               = 2000;
  private   static final long DEFAULT_STALE_CLIENT_CONNECTION_TIME = 10000;

  private String                                         transportType;
  private Category                                       log   = Log.createCategory(getClass());
  private Map<ServerAddress, SocketClientConnectionPool> pools = new ConcurrentHashMap<ServerAddress, SocketClientConnectionPool>();
  private volatile boolean                               poolCleanerStarted;
  
  protected SocketTransportProvider(String transportType) {
  	this.transportType = transportType;
  }
  
  public SocketTransportProvider() {
  	this(SOCKET_TRANSPORT_TYPE);
  }
  
  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(ServerAddress)
   */
  public synchronized Connections getPoolFor(ServerAddress address)
    throws RemoteException {
    Props props         = Props.getSystemProperties();
    long  resetInterval = props.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL);
    
    if(!poolCleanerStarted) {
      Hub.getModules().getTaskManager().addTask(
          new TaskContext(getClass().getSimpleName(), DEFAULT_STALE_CLIENT_CONNECTION_TIME), 
          new Task() {
            @Override
            public void exec(TaskContext ctx) {
              doCleanPools();
            }
          }
      );
      poolCleanerStarted = true;
    }
    
    SocketClientConnectionPool pool = pools.get(address);
    if(pool == null) {
      if(!(address instanceof TCPAddress)) {
        throw new IllegalArgumentException("Address not instance of TCPAddress: " + address);
      }
      
      TCPAddress tcpAddr = (TCPAddress) address;

      try {
        Class<?>  factoryClass = props.getClass(CLIENT_FACTORY, TcpSocketConnectionFactory.class);
        ConnectionFactory factory = (ConnectionFactory) factoryClass.newInstance();
        if(factory instanceof SocketRmiConnectionFactory) {
          ((SocketRmiConnectionFactory) factory).setResetInterval(resetInterval);
        }
        pool = new SocketClientConnectionPool(transportType, tcpAddr.getHost(), tcpAddr.getPort(), factory);
        pools.put(address, pool);        
      } catch (ClassNotFoundException e) {
        throw new RemoteException("Could load connection factory class (was not found)", e);
      } catch (IllegalAccessException e) {
        throw new RemoteException("Could not instantiate connection factory (illegal access, " +
                                  "make sure class has default, no-args, public constructor", e);
      } catch (InstantiationException e) {
        throw new RemoteException("Could not instantiate connection factory", e);
      }
    }

    return pool;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newServer(Properties)
   */
  public Server newServer(Properties props) throws RemoteException {
    Props pu = new Props().addProperties(props).addProperties(System.getProperties());
    return doNewServer(pu.getIntProperty(PORT, 0), pu);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newDefaultServer()
   */
  public Server newDefaultServer() throws RemoteException {
    Props pu = new Props().addProperties(System.getProperties());
    return doNewServer(pu.getIntProperty(PORT, 0), pu);
  }

  /**
   * @param port the port on which to start a new server.
   * @return a new {@link Server}.
   * @throws RemoteException
   */
  public Server newServer(int port) throws RemoteException{
    Props pu = new Props().addProperties(System.getProperties());
    return doNewServer(port, pu);
  }

  protected Server doNewServer(int port, Props props) throws RemoteException{
    int coreThreads = props.getIntProperty(Consts.SERVER_CORE_THREADS, ThreadingConfiguration.DEFAULT_CORE_POOL_SIZE);   
    int maxThreads  = props.getIntProperty(Consts.SERVER_MAX_THREADS, ThreadingConfiguration.DEFAULT_MAX_POOL_SIZE);  
    int queueSize   = props.getIntProperty(Consts.SERVER_THREADS_QUEUE_SIZE, ThreadingConfiguration.DEFAULT_QUEUE_SIZE); 
    long keepAlive  = props.getLongProperty(Consts.SERVER_THREADS_KEEP_ALIVE, ThreadingConfiguration.DEFAULT_KEEP_ALIVE.getValueInSeconds());

    ThreadingConfiguration threadConf = ThreadingConfiguration.newInstance()
        .setCorePoolSize(coreThreads)
        .setMaxPoolSize(maxThreads)
        .setQueueSize(queueSize)
        .setKeepAlive(Time.createSeconds(keepAlive));

    SocketRmiServer         server;
    long                    resetInterval; 
    String                  bindAddress = null;
    UbikServerSocketFactory serverSocketFactory = null;
    
    try{
      bindAddress = props.getProperty(BIND_ADDRESS, Localhost.getAnyLocalAddress().getHostAddress());
    }catch(IOException e){
      throw new RemoteException("Invalid bind address", e);
    }
    
    if(port == 0){
      try{
        port = new TcpPortSelector().select(bindAddress);
      }catch(IOException e){
        throw new RemoteException("Could not acquire random port");
      }
    }

    if (props.getProperty(SERVER_FACTORY) != null) {
      String factoryClassName = props.getProperty(SERVER_FACTORY);

      try {
        serverSocketFactory = (UbikServerSocketFactory) Class.forName(factoryClassName).newInstance();
      } catch (InstantiationException e) {
        throw new java.rmi.RemoteException("Problem instantiating server socket factory", e);
      } catch (IllegalAccessException e) {
        throw new java.rmi.RemoteException("Could not find server socket factory; " +
            "make sure factory class is public, and has a public, no-arg constructor", e);
      } catch (ClassNotFoundException e) {
        throw new java.rmi.RemoteException("Could not find server socket factory implementation: " + factoryClassName, e);
      }
    }
    
    resetInterval = props.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL);
    
    try {
      server = SocketRmiServer.Builder.create(transportType)
        .setBindAddress(bindAddress)
        .setThreadingConfig(threadConf)
        .setResetInterval(resetInterval)
        .setPort(port)
        .setServerSocketFactory(serverSocketFactory)
        .build();
    } catch (IOException e) {
      throw new RemoteException("Could not create server", e);
    }
    return server;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   */
  public String getTransportType() {
    return transportType;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  public void shutdown() {
    doCleanPools();
  }
  
  private void doCleanPools() {
    for(SocketClientConnectionPool pool : pools.values()) {
      if ((System.currentTimeMillis() - pool.internalPool().getLastUsageTime()) >= DEFAULT_STALE_CLIENT_CONNECTION_TIME) {
        log.debug("Shrinking socket client connection pool...");
        pool.internalPool().shrinkTo(0);
      }
    }
  }
}