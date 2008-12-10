package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.util.Properties;

import org.sapia.ubik.net.DefaultClientSocketFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.net.UbikServerSocketFactory;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.PropUtil;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.util.Localhost;


/**
 * Implements the <code>TransportProvider</code> interface over standard
 * java socket.
 *
 * @see java.net.Socket
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketTransportProvider implements TransportProvider {
  /**
   * Constant corresponding to this provider class' transport type.
   */
  public static final String TRANSPORT_TYPE = TCPAddress.TRANSPORT_TYPE;

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.max-threads</code>
   * system property.
   */
  public static final String MAX_THREADS = "ubik.rmi.transport.socket.max-threads";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.client-factory</code>
   * system property.
   */
  public static final String CLIENT_FACTORY = "ubik.rmi.transport.socket.client-factory";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.server-factory</code>
   * system property. It identifies the {@link UbikServerSocketFactory} implementation to use.
   *
   */
  public static final String SERVER_FACTORY = "ubik.rmi.transport.socket.server-factory";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.bind-address</code>
   * system property.
   */
  public static final String BIND_ADDRESS = "ubik.rmi.transport.socket.bind-address";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.socket.port</code>
   * system property.
   */
  public static final String     PORT = "ubik.rmi.transport.socket.port";
  
  public static final long DEFAULT_RESET_INTERVAL = 5000;
  
  private RMIClientSocketFactory _factory;
  private long _resetInterval = -1;

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getPoolFor(ServerAddress)
   */
  public synchronized Connections getPoolFor(ServerAddress address)
    throws RemoteException {
    if (_factory == null) {
      if (System.getProperty(CLIENT_FACTORY) != null) {
        String name = System.getProperty(CLIENT_FACTORY);

        try {
          _factory = (RMIClientSocketFactory) Class.forName(name).newInstance();
        } catch (InstantiationException e) {
          throw new java.rmi.RemoteException("problem instantiating client socket factory",
            e);
        } catch (IllegalAccessException e) {
          throw new java.rmi.RemoteException("could not find client socket factory; make sure factory class is public, and has a public, no-arg constructor",
            e);
        } catch (ClassNotFoundException e) {
          throw new java.rmi.RemoteException(
            "could not find client socket factory implementation: " + name, e);
        }
      } else {
        _factory = new DefaultClientSocketFactory();
      }
    }
    if(_resetInterval < 0){
      PropUtil pu = new PropUtil().addProperties(System.getProperties());
      _resetInterval = pu.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL);
    }
    return SocketClientConnectionPool.getInstance(address, _factory, _resetInterval);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newServer(Properties)
   */
  public Server newServer(Properties props) throws RemoteException {
    PropUtil pu = new PropUtil().addProperties(props).addProperties(System.getProperties());
    return doNewServer(pu.getIntProperty(PORT, 0), pu);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newDefaultServer()
   */
  public Server newDefaultServer() throws RemoteException {
    PropUtil pu = new PropUtil().addProperties(System.getProperties());
    return doNewServer(pu.getIntProperty(PORT, 0), pu);
  }
  
  public Server newServer(int port) throws RemoteException{
    PropUtil pu = new PropUtil().addProperties(System.getProperties());
    return doNewServer(port, pu);
  }

  protected Server doNewServer(int port, PropUtil pu) throws RemoteException{
    SocketRmiServer server;
    int             maxThreads = 0;
    
    try{
      maxThreads = pu.getIntProperty(Consts.SERVER_MAX_THREADS, 0);
    } catch (NumberFormatException e) {
      Log.error(getClass(),
        "could not parse integer from property: " +
        Consts.SERVER_MAX_THREADS);
    }
    if(maxThreads == 0){
      maxThreads = pu.getIntProperty(MAX_THREADS, 0);
    }
    
    String bindAddress = null;
    
    try{
      bindAddress = pu.getProperty(BIND_ADDRESS, Localhost.getLocalAddress().getHostAddress());
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

    if (pu.getProperty(SERVER_FACTORY) != null) {
      String                  name = pu.getProperty(SERVER_FACTORY);
      UbikServerSocketFactory fac;

      try {
        fac = (UbikServerSocketFactory) Class.forName(name).newInstance();
      } catch (InstantiationException e) {
        throw new java.rmi.RemoteException("problem instantiating server socket factory",
          e);
      } catch (IllegalAccessException e) {
        throw new java.rmi.RemoteException("could not find server socket factory; make sure factory class is public, and has a public, no-arg constructor",
          e);
      } catch (ClassNotFoundException e) {
        throw new java.rmi.RemoteException(
          "could not find server socket factory implementation: " + name, e);
      }

      try {
        server = new SocketRmiServer(
            bindAddress, 
            port, 
            maxThreads,
            pu.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL),
            fac);
      } catch (IOException e) {
        throw new RemoteException("could not create server socket", e);
      }
    } else {
      try {
        server = new SocketRmiServer(
            bindAddress, 
            port, 
            maxThreads,
            pu.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL));
      } catch (IOException e) {
        throw new RemoteException("could not create server socket", e);
      }
    }
    if(pu.getBooleanProperty(Consts.STATS_ENABLED, false)){
      server.enableStats();
    }
    else{
      server.disableStats();
    }
    return server;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#getTransportType()
   */
  public String getTransportType() {
    return TRANSPORT_TYPE;
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#shutdown()
   */
  public void shutdown() {
    SocketClientConnectionPool.shutdown();
  }
}
