package org.sapia.ubik.net;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Props;

/**
 * Implements a factory of {@link SocketConnection} instances. An instance of this class
 * may be provided with a {@link RMIClientSocketFactory}, which will then be internally
 * used to create {@link Socket}s.
 *
 * @author Yanick Duchesne
 */
public class SocketConnectionFactory implements ConnectionFactory {
  
	
  private static final int NO_SO_TIMEOUT = 0;
  
	private int 										 bufsize = Props.getSystemProperties().getIntProperty(
																						 		Consts.MARSHALLING_BUFSIZE, 
                                              	Consts.DEFAULT_MARSHALLING_BUFSIZE
                                             ); 
	
	protected String                 transportType;
	protected ClassLoader            loader;
  private   int                    soTimeout;
  protected RMIClientSocketFactory clientSocketFactory;

  /**
   * Creates an instance of this class with the current thread's {@link ClassLoader}.
   */
  public SocketConnectionFactory(String transportType) {
    this(transportType, new DefaultRMIClientSocketFactory(), Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates an instance of this class with the current thread's {@link ClassLoader}
   * and given {@link RMIClientSocketFactory}.
   * 
   * @param client a {@link RMIClientSocketFactory}.
   */
  public SocketConnectionFactory(String transportType, RMIClientSocketFactory client) {
    this(transportType, client, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates an instance of this class with the given {@link RMIClientSocketFactory} and 
   * {@link ClassLoader}.
   * 
   * @param client a {@link RMIClientSocketFactory}
   * @param loader a {@link ClassLoader}
   */
  public SocketConnectionFactory(String transportType, RMIClientSocketFactory client, ClassLoader loader) {
    this(loader);
    this.transportType  = transportType;
    clientSocketFactory = client;
  }

  /**
   * Creates an instance of this class with the given {@link ClassLoader}.
   * 
   * @param loader a {@link ClassLoader}.
   */
  public SocketConnectionFactory(ClassLoader loader) {
    this.loader = loader;
  }
  
  /**
   * @param soTimeout the SO_TIMEOUT that should be assigned to sockets
   * that this instance returns.
   * @see Socket#setSoTimeout(int)
   */
  public void setSoTimeout(int soTimeout) {
    this.soTimeout = soTimeout;
  }

  /**
   * @see org.sapia.ubik.net.ConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port)
    throws IOException {
    Socket socket;
    if (clientSocketFactory == null) {
      socket = new Socket(host, port);
    } else {
      socket = clientSocketFactory.createSocket(host, port);
    }
    if(soTimeout > NO_SO_TIMEOUT) {
      socket.setSoTimeout(soTimeout);
    }      
    return new SocketConnection(transportType, socket, loader, bufsize);    
  }
  
  /**
   * Creates a new {@link Connection} around the given socket.
   *
   * @see org.sapia.ubik.net.ConnectionFactory#newConnection(String, int)
   * @return a {@link SocketConnection}.
   */
  public Connection newConnection(Socket sock) throws IOException {
    return new SocketConnection(transportType, sock, loader, bufsize);
  }
  
  @Override
  public String getTransportType() {
    return transportType;
  }
}
