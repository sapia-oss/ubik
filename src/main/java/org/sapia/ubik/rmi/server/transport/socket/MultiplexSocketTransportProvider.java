package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.net.mplex.MultiplexServerSocket;
import org.sapia.ubik.net.mplex.MultiplexSocketConnector;
import org.sapia.ubik.net.mplex.StreamSelector;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.PropUtil;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.util.Localhost;


/**
 * Implements the <code>TransportProvider</code> interface by extending the basic
 * socket transport provider. It adds the functionality of receiving data other than
 * objects from Ubik's command protocol that encapsulate remote calls. Through
 * this transport provider you can create a connector that will handle incoming
 * socket connections for a specific type of transport protocol.
 * 
 * @see SocketTransportProvider
 * @see org.sapia.ubik.net.mplex.MultiplexSocket
 * @see org.sapia.ubik.net.mplex.MultiplexServerSocket
 * @see org.sapia.ubik.net.mplex.StreamSelector
 * 
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MultiplexSocketTransportProvider extends SocketTransportProvider {
  /**
   * This constant corresponds to the <code>ubik.rmi.transport.mplex.acceptor-threads</code>
   * property.
   */
  public static final String ACCEPTOR_THREADS = "ubik.rmi.transport.mplex.acceptor-threads";

  /**
   * This constant corresponds to the <code>ubik.rmi.transport.mplex.selector-threads</code>
   * property.
   */
  public static final String SELECTOR_THREADS = "ubik.rmi.transport.mplex.selector-threads";

  /** The reference on the multiplex Server socket. */
  private MultiplexServerSocket _multiplexServer;

  /**
   * Creates a new MultiplexSocketTransportProvider instance.
   */
  public MultiplexSocketTransportProvider() {
    super();
  }

  /**
   * Creates a new socket connector for the stream selector passed in.
   *
   * @param aSelector The stream selector of the connector to create.
   * @return The created socket connector.
   */
  public MultiplexSocketConnector createSocketConnector(
    StreamSelector aSelector) {
    if (_multiplexServer == null) {
      throw new IllegalStateException(
        "Could not create a connector - no multiplex server is created");
    }

    return _multiplexServer.createSocketConnector(aSelector);
  }

  /**
   * Removes the passed in connector from the multiplex server.
   *
   * @param anInterceptor The connector to remove.
   */
  public void removeSocketConnector(MultiplexSocketConnector anInterceptor) {
    _multiplexServer.removeSocketConnector(anInterceptor);
  }

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newServer(Properties)
   */
  public Server newServer(Properties props) throws RemoteException {
    PropUtil pu = new PropUtil().addProperties(props).addProperties(System.getProperties());
    return doNewServer(pu.getIntProperty(PORT, 0), pu);
  }
  
  public Server newServer(int port) throws RemoteException {
    PropUtil pu = new PropUtil().addProperties(System.getProperties());
    return doNewServer(port, pu);
  }
  
  protected Server doNewServer(int port, PropUtil pu) throws RemoteException{
    SocketRmiServer server;
    int             maxThreads    = 0;
    int             acceptorCount = 0;
    int             selectorCount = 0;
    
    if (_multiplexServer != null) {
      throw new IllegalStateException(
        "There is already one multiplex server running: " + _multiplexServer);
    }
    
    maxThreads = pu.getIntProperty(Consts.SERVER_MAX_THREADS, 0);
    
    if (maxThreads == 0) {
      maxThreads = pu.getIntProperty(MAX_THREADS, 0);
    }
    
    acceptorCount = pu.getIntProperty(ACCEPTOR_THREADS, 0);
    
    selectorCount = pu.getIntProperty(SELECTOR_THREADS, 0);
    
    String bindAddress = null;
    
    try{
      bindAddress = pu.getProperty(BIND_ADDRESS, Localhost.getAnyLocalAddress().getHostAddress());
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

    try {
      Log.debug(getClass(), "Creating server on " + bindAddress + ":" + port);
      if (Localhost.isIpPatternDefined()) {
        _multiplexServer = new MultiplexServerSocket(port, 50, InetAddress.getByName(bindAddress));
      } else {
        _multiplexServer = new MultiplexServerSocket(port, 50);
      }

      if (acceptorCount > 0) {
        _multiplexServer.setAcceptorDaemonThread(acceptorCount);
      }

      if (selectorCount > 0) {
        _multiplexServer.setSelectorDaemonThread(selectorCount);
      }

      server = new SocketRmiServer(maxThreads, _multiplexServer, pu.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL));
    } catch (IOException ioe) {
      throw new RemoteException("could not create multiplex server socket", ioe);
    }

    if(pu.getBooleanProperty(Consts.STATS_ENABLED, false)){
      server.enableStats();
    }
    else{
      server.disableStats();
    }    
    return server;
  }
}
