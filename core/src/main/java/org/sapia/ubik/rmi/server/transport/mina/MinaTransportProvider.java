package org.sapia.ubik.rmi.server.transport.mina;

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
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Props;
import org.sapia.ubik.util.Time;

/**
 * This transport provider is implemented on top of the <a
 * href="http://mina.apache.org">Mina</a> framework.
 * <p>
 * It internally creates {@link MinaServer} instances. Various configuration
 * properties are "understood" by this provider (see the doc for the
 * corresponding constants further below). In addition, this provider interprets
 * the <code>ubik.rmi.server.max-threads</code> property as indicating the
 * number of processor threads that should be created by a {@link MinaServer}
 * instance.
 * 
 * @see Consts#SERVER_CORE_THREADS
 * @see Consts#SERVER_MAX_THREADS
 * @see Consts#SERVER_THREADS_QUEUE_SIZE
 * @see Consts#SERVER_THREADS_KEEP_ALIVE
 * 
 * @author Yanick Duchesne
 * 
 */
public class MinaTransportProvider implements TransportProvider {

  /**
   * Constant corresponding to this provider class' transport type.
   */
  public static final String TRANSPORT_TYPE = "nio/tcp/mina";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.transport.nio.mina.bind-address</code> system property.
   */
  public static final String BIND_ADDRESS = "ubik.rmi.transport.nio.mina.bind-address";

  /**
   * This constant corresponds to the
   * <code>ubik.rmi.transport.nio.mina.port</code> system property.
   */
  public static final String PORT = "ubik.rmi.transport.nio.mina.port";

  private int bufsize = Props.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);

  private Map<ServerAddress, MinaRmiClientConnectionPool> pools = new ConcurrentHashMap<ServerAddress, MinaRmiClientConnectionPool>();

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(org.sapia.ubik.net.ServerAddress)
   */
  public synchronized Connections getPoolFor(ServerAddress address) throws RemoteException {
    MinaRmiClientConnectionPool pool = (MinaRmiClientConnectionPool) pools.get(address);

    if (pool == null) {
      pool = new MinaRmiClientConnectionPool(((MinaAddress) address).getHost(), ((MinaAddress) address).getPort(), bufsize);
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
    if (props.getProperty(PORT) != null) {
      try {
        port = Integer.parseInt(props.getProperty(PORT));
      } catch (NumberFormatException e) {
        Log.error(getClass(), "Could not parse integer from property " + BIND_ADDRESS + ": " + PORT);
      }
    }
    InetSocketAddress addr;
    String bindAddress = props.getProperty(BIND_ADDRESS);
    if (bindAddress != null) {
      addr = new InetSocketAddress(bindAddress, port);
    } else {
      try {
        if (Localhost.isIpPatternDefined()) {
          addr = new InetSocketAddress(Localhost.getAnyLocalAddress().getHostAddress(), port);
        } else {
          addr = new InetSocketAddress(port);
        }
      } catch (UnknownHostException e) {
        throw new RemoteException("Could not determine local address", e);
      }
    }

    int specificBufsize = pu.getIntProperty(Consts.MARSHALLING_BUFSIZE, this.bufsize);

    int coreThreads = pu.getIntProperty(Consts.SERVER_CORE_THREADS, ThreadingConfiguration.DEFAULT_CORE_POOL_SIZE);
    int maxThreads = pu.getIntProperty(Consts.SERVER_MAX_THREADS, ThreadingConfiguration.DEFAULT_MAX_POOL_SIZE);
    int queueSize = pu.getIntProperty(Consts.SERVER_THREADS_QUEUE_SIZE, ThreadingConfiguration.DEFAULT_QUEUE_SIZE);
    long keepAlive = pu.getLongProperty(Consts.SERVER_THREADS_KEEP_ALIVE, ThreadingConfiguration.DEFAULT_KEEP_ALIVE.getValueInSeconds());

    ThreadingConfiguration threadConf = ThreadingConfiguration.newInstance().setCorePoolSize(coreThreads).setMaxPoolSize(maxThreads)
        .setQueueSize(queueSize).setKeepAlive(Time.createSeconds(keepAlive));

    try {
      MinaServer server = new MinaServer(addr, specificBufsize, threadConf);
      return server;
    } catch (IOException e) {
      throw new RemoteException("Could not create server", e);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  public synchronized void shutdown() {
    for (MinaRmiClientConnectionPool pool : pools.values()) {
      pool.internalPool().shrinkTo(0);
    }
  }

}
