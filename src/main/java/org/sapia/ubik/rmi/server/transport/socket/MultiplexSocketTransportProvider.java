package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.net.ThreadPool;
import org.sapia.ubik.net.UbikServerSocketFactory;
import org.sapia.ubik.net.mplex.MultiplexServerSocket;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Props;


/**
 * Implements the {@link TransportProvider} interface by extending the basic
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
 */
public class MultiplexSocketTransportProvider extends SocketTransportProvider {
  
  /**
   * Constant corresponding to this provider class' transport type.
   */
  public static final String MPLEX_TRANSPORT_TYPE   = "tcp/mplex";
	
  /**
   * The number of backlog connections.
   */
  private static final int   BACKLOG          = 50;
  
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

  private Category log = Log.createCategory(getClass());
  
  /**
   * Creates a new MultiplexSocketTransportProvider instance.
   */
  public MultiplexSocketTransportProvider() {
    super(MPLEX_TRANSPORT_TYPE);
  }

  /**
   * Creates a new socket connector for the stream selector passed in.
   *
   * @param aSelector The stream selector of the connector to create.
   * @return The created socket connector.
   */
  /*public MultiplexSocketConnector createSocketConnector(StreamSelector aSelector) {
    
    Multipl
    if (multiplexServer == null) {
      throw new IllegalStateException(
        "Could not create a connector - no multiplex server is created");
    }

    return multiplexServer.createSocketConnector(aSelector);
  }*/

  /**
   * Removes the passed in connector from the multiplex server.
   *
   * @param anInterceptor The connector to remove.
   */
  /*public void removeSocketConnector(MultiplexSocketConnector anInterceptor) {
    multiplexServer.removeSocketConnector(anInterceptor);
  }*/

  /**
   * @see org.sapia.ubik.rmi.server.transport.TransportProvider#newServer(Properties)
   */
  public Server newServer(Properties properties) throws RemoteException {
    Props props = new Props().addProperties(properties).addProperties(System.getProperties());
    return doNewServer(props.getIntProperty(PORT, 0), props);
  }
  
  public Server newServer(int port) throws RemoteException {
    Props props = Props.getSystemProperties();
    return doNewServer(port, props);
  }
  
  protected Server doNewServer(int port, Props props) throws RemoteException{
    int maxThreads;
    int acceptorCount;
    int selectorCount;
        
    maxThreads = props.getIntProperty(Consts.SERVER_MAX_THREADS, ThreadPool.NO_MAX);   
    if (maxThreads == 0) {
      maxThreads = props.getIntProperty(MAX_THREADS, ThreadPool.NO_MAX);
    }
    
    acceptorCount = props.getIntProperty(ACCEPTOR_THREADS, 0);
    
    selectorCount = props.getIntProperty(SELECTOR_THREADS, 0);
    
    String bindAddress = null;
    try{
      bindAddress = props.getProperty(BIND_ADDRESS);
      if(bindAddress == null && Localhost.isIpPatternDefined()) {
        bindAddress = Localhost.getAnyLocalAddress().getHostAddress();
      }
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
      log.debug("Creating server on %s:%s", bindAddress, port);
      
      return SocketRmiServer.Builder.create(MPLEX_TRANSPORT_TYPE)
        .setBindAddress(bindAddress)
        .setPort(port)
        .setMaxThreads(maxThreads)
        .setConnectionFactory(new MultiplexSocketConnectionFactory())
        .setServerSocketFactory(new MultiPlexServerSocketFactory(acceptorCount, selectorCount))
        .setResetInterval(props.getLongProperty(Consts.SERVER_RESET_INTERVAL, DEFAULT_RESET_INTERVAL))
        .build();
    } catch (IOException ioe) {
      throw new RemoteException("Could not create multiplex server socket", ioe);
    }
  }
  
  // --------------------------------------------------------------------------
  
  static class MultiPlexServerSocketFactory implements UbikServerSocketFactory {
    
    private int acceptorCount;
    private int selectorCount;
    
    MultiPlexServerSocketFactory(int acceptorCount, int selectorCount) {
      this.acceptorCount = acceptorCount;
      this.selectorCount = selectorCount;
    }
  
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
      return init(new MultiplexServerSocket(port, BACKLOG));
    }
    
    @Override
    public ServerSocket createServerSocket(int port, String bindAddr)
        throws IOException {
      return init(new MultiplexServerSocket(port, BACKLOG, InetAddress.getByName(bindAddr)));
    }
    
    private MultiplexServerSocket init(MultiplexServerSocket socket) {
      if (acceptorCount > 0) {
        socket.setAcceptorDaemonThread(acceptorCount);
      }

      if (selectorCount > 0) {
        socket.setSelectorDaemonThread(selectorCount);
      }
      
      return socket;
    }
  }
}
