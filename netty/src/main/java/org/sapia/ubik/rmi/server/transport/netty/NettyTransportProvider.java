package org.sapia.ubik.rmi.server.transport.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.net.netty.NettyAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Localhost;

/**
 * A {@link TransportProvider} implementation on top of the Netty server
 * framework.
 *
 * @author yduchesne
 *
 */
public class NettyTransportProvider implements TransportProvider, NettyConsts {

  private Category log = Log.createCategory(getClass());

  /**
   * Constant corresponding to this provider class' transport type.
   */
  public static final String TRANSPORT_TYPE = "nio/tcp/netty";

  private Map<ServerAddress, NettyClientConnectionPool> pools = new ConcurrentHashMap<ServerAddress, NettyClientConnectionPool>();

  private int bufsize = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Defaults.DEFAULT_MARSHALLING_BUFSIZE);

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(org.sapia.ubik.net.ServerAddress)
   */
  @Override
  public synchronized Connections getPoolFor(ServerAddress address) throws RemoteException {
    NettyClientConnectionPool pool = pools.get(address);

    if (pool == null) {
      NettyAddress nettyAddr = (NettyAddress) address;
      pool = new NettyClientConnectionPool(nettyAddr.getHost(), nettyAddr.getPort(), bufsize);
      pools.put(address, pool);
    }

    return pool;
  }

  @Override
  public String getTransportType() {
    return TRANSPORT_TYPE;
  }

  @Override
  public Server newDefaultServer() throws RemoteException {
    Properties props = new Properties(System.getProperties());
    return newServer(props);
  }

  @Override
  public Server newServer(Properties props) throws RemoteException {
    Conf config = new Conf().addProperties(props).addProperties(System.getProperties());
    int port = 0;
    if (config.getProperty(SERVER_PORT_KEY) != null) {
      try {
        port = Integer.parseInt(config.getProperty(SERVER_PORT_KEY));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Could not parse integer from property " + SERVER_PORT_KEY + ": " + config.getProperty(SERVER_PORT_KEY));
      }
    }

    InetSocketAddress addr;
    String bindAddress = config.getProperty(SERVER_BIND_ADDRESS_KEY);
    try {
      if (bindAddress != null) {
        addr = new InetSocketAddress(bindAddress, port == 0 ? new TcpPortSelector().select() : port);
      } else {
        bindAddress = Localhost.getPreferredLocalAddress().getHostAddress();
        addr = new InetSocketAddress(bindAddress, port == 0 ? new TcpPortSelector().select() : port);
      }
    } catch (UnknownHostException e) {
      throw new RemoteException("Could not determine server bind address", e);
    } catch (IOException e) {
      throw new RemoteException("Could not determine server bind address", e);
    }

    log.debug("Server bind address %s", bindAddress);
    
    int selectorThreads = config.getIntProperty(Consts.SERVER_INBOUND_THREADS, Defaults.DEFAULT_INBOUND_THREADS);

    return new NettyServer(
        new NettyAddress(addr.getAddress().getHostAddress(), addr.getPort()), 
        Hub.getModules().getClientRuntime().getDispatcher(),
        Threads.createIoInboundPool("netty", selectorThreads), 
        Threads.createWorkerPool()
     );
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  @Override
  public synchronized void shutdown() {
    for (NettyClientConnectionPool pool : pools.values()) {
      pool.internalPool().shrinkTo(0);
    }
  }

}
