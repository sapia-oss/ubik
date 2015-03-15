package org.sapia.ubik.rmi.server.transport.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.net.netty.NettyAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.TimeValue;

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
  public static final String TRANSPORT_TYPE = "tcp/nio/netty";

  private Map<ServerAddress, NettyClientConnectionPool> pools = new ConcurrentHashMap<ServerAddress, NettyClientConnectionPool>();

  private int bufsize = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);

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

    ThreadingConfiguration workerConf = ThreadingConfiguration
        .newInstance()
        .setCorePoolSize(config.getIntProperty(Consts.SERVER_CORE_THREADS, ThreadingConfiguration.DEFAULT_CORE_POOL_SIZE))
        .setMaxPoolSize(config.getIntProperty(Consts.SERVER_MAX_THREADS, ThreadingConfiguration.DEFAULT_MAX_POOL_SIZE))
        .setQueueSize(config.getIntProperty(Consts.SERVER_THREADS_QUEUE_SIZE, ThreadingConfiguration.DEFAULT_QUEUE_SIZE))
        .setKeepAlive(
            TimeValue.createSeconds(config.getLongProperty(Consts.SERVER_THREADS_KEEP_ALIVE, ThreadingConfiguration.DEFAULT_KEEP_ALIVE.getValueInSeconds())));

    ThreadingConfiguration ioConf = ThreadingConfiguration.newInstance()
        .setCorePoolSize(config.getIntProperty(SERVER_IO_CORE_THREADS_KEY, DEFAULT_SERVER_IO_CORE_THREADS))
        .setMaxPoolSize(config.getIntProperty(SERVER_IO_MAX_THREADS_KEY, DEFAULT_SERVER_IO_MAX_THREADS))
        .setQueueSize(config.getIntProperty(SERVER_IO_QUEUE_SIZE_KEY, DEFAULT_SERVER_IO_QUEUE_SIZE))
        .setKeepAlive(TimeValue.createSeconds(config.getLongProperty(SERVER_IO_KEEP_ALIVE_KEY, DEFAULT_SERVER_IO_KEEP_ALIVE)));

    return new NettyServer(new NettyAddress(addr.getAddress().getHostAddress(), addr.getPort()), Hub.getModules().getClientRuntime().getDispatcher(),
        ioConf, workerConf);
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
