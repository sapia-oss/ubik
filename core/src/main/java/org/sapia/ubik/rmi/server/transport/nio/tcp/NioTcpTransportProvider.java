package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
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
 * This transport provider is implemented on top of the <a href="http://mina.apache.org">Mina</a> 
 * framework.  
 * <p>
 * It internally creates {@link NioServer} instances. Various configuration 
 * properties are "understood" by this provider (see the doc for the corresponding 
 * constants further below). In addition, this provider interprets the 
 * <code>ubik.rmi.server.max-threads</code> property as indicating the number of 
 * processor threads that should be created by a {@link NioServer} instance (see
 * {@link NioServer#NioServer(InetSocketAddress, int, int)}).
 * <p>
 * 
 * @see Consts#SERVER_MAX_THREADS
 *  
 * @author Yanick Duchesne
 * 
 */
public class NioTcpTransportProvider implements TransportProvider {

  /**
   * Constant corresponding to this provider class' transport type.
   */
  public static final String TRANSPORT_TYPE = "tcp/nio";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.transport.nio-tcp.bind-address</code> system property.
   */
  public static final String BIND_ADDRESS   = "ubik.rmi.transport.nio-tcp.bind-address";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.transport.nio-tcp.port</code> system property.
   */
  public static final String PORT           = "ubik.rmi.transport.nio-tcp.port";
  
  private int bufsize = Props.getSystemProperties().getIntProperty(
  												Consts.MARSHALLING_BUFSIZE, 
  												Consts.DEFAULT_MARSHALLING_BUFSIZE
  											);
  
  private volatile boolean started;
  
  private Map<ServerAddress, NioTcpClientConnectionPool> pools = new ConcurrentHashMap<ServerAddress, NioTcpClientConnectionPool>();

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(org.sapia.ubik.net.ServerAddress)
   */
  public synchronized Connections getPoolFor(ServerAddress address) throws RemoteException {
    NioTcpClientConnectionPool pool = (NioTcpClientConnectionPool) pools.get(address);

    if(pool == null) {
      pool = new NioTcpClientConnectionPool(((NioAddress) address).getHost(),
          ((NioAddress) address).getPort(), bufsize);
      pools.put(address, pool);
    }

    return pool;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   */
  public String getTransportType() {
    return TRANSPORT_TYPE;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newDefaultServer()
   */
  public Server newDefaultServer() throws RemoteException {
    return newServer(System.getProperties());
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newServer(java.util.Properties)
   */
  public Server newServer(Properties props) throws RemoteException {
    Props pu = new Props().addProperties(props).addProperties(System.getProperties());  
    int port = 0;
    if(props.getProperty(PORT) != null) {
      try {
        port = Integer.parseInt(props.getProperty(PORT));
      } catch(NumberFormatException e) {
        Log.error(getClass(), "Could not parse integer from property " + BIND_ADDRESS + ": " + PORT);
      }
    }
    InetSocketAddress addr;
    String bindAddress = props.getProperty(BIND_ADDRESS);
    if(bindAddress != null) {
      addr = new InetSocketAddress(bindAddress, port);
    } else {
      try{
        if (Localhost.isIpPatternDefined()) {
          addr = new InetSocketAddress(Localhost.getAnyLocalAddress().getHostAddress(), port);
        } else {
          addr = new InetSocketAddress(port);
        }
      }catch(UnknownHostException e){
        throw new RemoteException("Could not determine local address", e);
      }
    }
    
    int specificBufsize = pu.getIntProperty(Consts.MARSHALLING_BUFSIZE, this.bufsize);

    int coreThreads = pu.getIntProperty(Consts.SERVER_CORE_THREADS, ThreadingConfiguration.DEFAULT_CORE_POOL_SIZE);   
    int maxThreads  = pu.getIntProperty(Consts.SERVER_MAX_THREADS, ThreadingConfiguration.DEFAULT_MAX_POOL_SIZE);  
    int queueSize   = pu.getIntProperty(Consts.SERVER_THREADS_QUEUE_SIZE, ThreadingConfiguration.DEFAULT_QUEUE_SIZE); 
    long keepAlive  = pu.getLongProperty(Consts.SERVER_THREADS_KEEP_ALIVE, ThreadingConfiguration.DEFAULT_KEEP_ALIVE.getValueInSeconds());
    
    ThreadingConfiguration threadConf = ThreadingConfiguration.newInstance()
        .setCorePoolSize(coreThreads)
        .setMaxPoolSize(maxThreads)
        .setQueueSize(queueSize)
        .setKeepAlive(Time.createSeconds(keepAlive));
    
    try{
      NioServer server = new NioServer(addr, specificBufsize, threadConf);
      return server;
    }catch(IOException e){
      throw new RemoteException("Could not create server", e);
    } finally {
      if(!started) {
      	synchronized(this) {
          if(!started) {
            started = true;
            Hub.getModules().getTaskManager().addTask(
                new TaskContext(NioTcpClientConnectionPool.class.getName(), PoolCleaner.INTERVAL),
                new PoolCleaner(pools)
            );
          }
      	}
      }
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  public synchronized void shutdown() {
    for(NioTcpClientConnectionPool pool : pools.values()) {
      pool.internalPool().shrinkTo(0);
    }
  }
  
  // --------------------------------------------------------------------------
  
  static final class PoolCleaner implements Task {
    
    static final long INTERVAL = 5000;
    
    Map<ServerAddress, NioTcpClientConnectionPool> pools;

    PoolCleaner(Map<ServerAddress, NioTcpClientConnectionPool> pools) {
      this.pools = pools;
    }

    public void exec(TaskContext context) {
      NioTcpClientConnectionPool[] toClean;

      toClean = (NioTcpClientConnectionPool[]) pools.values().toArray(new NioTcpClientConnectionPool[pools.size()]);

      for(int i = 0; i < toClean.length; i++) {
        if((System.currentTimeMillis() - toClean[i].internalPool()
            .getLastUsageTime()) > INTERVAL) {
          Log.debug(getClass(), "Shrinking nio socket client connection pool...");
          toClean[i].internalPool().shrinkTo(0);
        }
      }
    }
  }
}
