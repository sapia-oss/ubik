package org.sapia.ubik.rmi.server.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Localhost;

/**
 * This transport provider is implemented on top of the <a href="http://mina.apache.org">Mina</a> 
 * framework.
 * <p>It internally creates {@link MinaServer} instances. Various configuration
 * properties are "understood" by this provider (see the doc for the
 * corresponding constants further below). In addition, this provider interprets
 * the <code>ubik.rmi.server.io.threads</code> property as indicating the
 * number of NIO selector threads that should be created by a {@link MinaServer}
 * instance.
 * 
 * @author yduchesne
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
  
  private Map<ServerAddress, MinaRmiClientConnectionPool> pools = new ConcurrentHashMap<ServerAddress, MinaRmiClientConnectionPool>();
 
  private int bufSize  = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Defaults.DEFAULT_MARSHALLING_BUFSIZE);


  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(org.sapia.ubik.net.ServerAddress)
   */
  public synchronized Connections getPoolFor(ServerAddress address) throws RemoteException {
    MinaRmiClientConnectionPool pool = (MinaRmiClientConnectionPool) pools.get(address);

    if (pool == null) {
      pool = new MinaRmiClientConnectionPool(((MinaAddress) address).getHost(), ((MinaAddress) address).getPort(), bufSize);
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
    Conf fullProps = new Conf().addProperties(props).addProperties(System.getProperties());
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
          addr = new InetSocketAddress(Localhost.getPreferredLocalAddress().getHostAddress(), port);
        } else {
          addr = new InetSocketAddress(port);
        }
      } catch (UnknownHostException e) {
        throw new RemoteException("Could not determine local address", e);
      }
    }

    int selectorThreads = fullProps.getIntProperty(Consts.SERVER_INBOUND_THREADS, Defaults.DEFAULT_INBOUND_THREADS);
    int specificBufSize = fullProps.getIntProperty(Consts.MARSHALLING_BUFSIZE, Defaults.DEFAULT_MARSHALLING_BUFSIZE);

    try {
      MinaServer server = new MinaServer(addr, specificBufSize, Threads.createIoInboundPool("mina", selectorThreads), Threads.createWorkerPool());
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
