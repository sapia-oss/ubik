package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.PropUtil;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.util.Localhost;

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
  public static final String TRANSPORT_TYPE = NioAddress.TRANSPORT_TYPE;

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
  
  /**
   * This constant corresponds to the
   * <code>ubik.rmi.transport.nio-tcp.buffer-size</code> system property.
   */
  public static final String BUFFER_SIZE    = "ubik.rmi.transport.nio-tcp.buffer-size";
  
  /**
   * The default buffer size (4000).
   * 
   * @see #BUFFER_SIZE
   */
  public static final int DEFAULT_BUFFER_SIZE = 4000;
  
  private int _bufsize = DEFAULT_BUFFER_SIZE;

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(org.sapia.ubik.net.ServerAddress)
   */
  public Connections getPoolFor(ServerAddress address) throws RemoteException {
    return NioTcpClientConnectionPool.getInstance(address, _bufsize);
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
    PropUtil pu = new PropUtil().addProperties(props).addProperties(System.getProperties());  
    int port = 0;
    if(props.getProperty(PORT) != null) {
      try {
        port = Integer.parseInt(props.getProperty(PORT));
      } catch(NumberFormatException e) {
        Log.error(getClass(), "Could not parse integer from property "
            + BIND_ADDRESS + ": " + PORT);
      }
    }
    InetSocketAddress addr;
    String bindAddress = props.getProperty(BIND_ADDRESS);
    if(bindAddress != null) {
      addr = new InetSocketAddress(bindAddress, port);
    } else {
      try{
        addr = new InetSocketAddress(Localhost.getLocalAddress().getHostAddress(), port);
      }catch(UnknownHostException e){
        throw new RemoteException("Could not determine local address", e);
      }
    }
    
    _bufsize = pu.getIntProperty(BUFFER_SIZE, DEFAULT_BUFFER_SIZE);
    int maxThreads = pu.getIntProperty(Consts.SERVER_MAX_THREADS, 0);
    
    try{
      NioServer server = new NioServer(addr, _bufsize, maxThreads);
      return server;
    }catch(IOException e){
      throw new RemoteException("Could not create server", e);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  public void shutdown() {
    NioTcpClientConnectionPool.shutdown();
  }
}
