package org.sapia.ubik.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.sapia.ubik.concurrent.Spawn;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.IoUtils;

/**
 * Implements a factory of {@link SocketConnection} instances. An instance of
 * this class may be provided with a {@link RMIClientSocketFactory}, which will
 * then be internally used to create {@link Socket}s.
 * 
 * @author yduchesne
 */
public class SocketConnectionFactory implements ConnectionFactory {

  protected Category log = Log.createCategory(getClass());

  private static final int NO_SO_TIMEOUT = 0;

  protected int bufsize = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);
  protected int connectionTimeout = Conf.getSystemProperties().getIntProperty(Consts.CLIENT_CONNECTION_TIMEOUT, Consts.DEFAULT_CLIENT_CONNECTION_TIMEOUT);
  protected int connectionMaxRetry = Conf.getSystemProperties().getIntProperty(Consts.CLIENT_CONNECTION_MAX_RETRY, Consts.DEFAULT_CLIENT_CONNECTION_MAX_RETRY);

  protected String transportType;
  protected ClassLoader loader;
  private int soTimeout;
  protected RMIClientSocketFactory clientSocketFactory;

  /**
   * Creates an instance of this class with the current thread's
   * {@link ClassLoader}.
   */
  public SocketConnectionFactory(String transportType) {
    this(transportType, new DefaultRMIClientSocketFactory(), Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates an instance of this class with the current thread's
   * {@link ClassLoader} and given {@link RMIClientSocketFactory}.
   * 
   * @param client
   *          a {@link RMIClientSocketFactory}.
   */
  public SocketConnectionFactory(String transportType, RMIClientSocketFactory client) {
    this(transportType, client, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Creates an instance of this class with the given
   * {@link RMIClientSocketFactory} and {@link ClassLoader}.
   * 
   * @param client
   *          a {@link RMIClientSocketFactory}
   * @param loader
   *          a {@link ClassLoader}
   */
  public SocketConnectionFactory(String transportType, RMIClientSocketFactory client, ClassLoader loader) {
    this(loader);
    this.transportType = transportType;
    clientSocketFactory = client;
  }

  /**
   * Creates an instance of this class with the given {@link ClassLoader}.
   * 
   * @param loader
   *          a {@link ClassLoader}.
   */
  public SocketConnectionFactory(ClassLoader loader) {
    this.loader = loader;
  }

  /**
   * @param soTimeout
   *          the SO_TIMEOUT that should be assigned to sockets that this
   *          instance returns.
   * @see Socket#setSoTimeout(int)
   */
  public void setSoTimeout(int soTimeout) {
    this.soTimeout = soTimeout;
  }

  /**
   * @see org.sapia.ubik.net.ConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port) throws IOException {
    Socket socket;
    if (clientSocketFactory == null) {
      socket = newSocket(host, port);
    } else {
      socket = clientSocketFactory.createSocket(host, port);
    }
    if (soTimeout > NO_SO_TIMEOUT) {
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
  
  protected Socket newSocket(String host, int port) throws RemoteException {
    Socket toReturn = null;
    int retryCount = 0;
    IOException lastException = null;
    do {
      log.debug("Attempting client socket connection to %s:%s (retry attempt: %s)", host, port, retryCount + 1);
      
      try {
        Socket tmp = newSocketAsync(host, port);
        toReturn = tmp;
        log.debug("Connection established to %s:%s", host, port);
      } catch (IOException e) {
        log.warning("Error attempting to connect to %s:%s. Retry count currently is: %s. Max retries set to: %s", e, retryCount, connectionMaxRetry);
        lastException = e;        
      }
      retryCount++;
    } while (toReturn == null && retryCount < connectionMaxRetry);
    if (lastException != null) {
      throw new RemoteException(String.format("Could not establish client connection to %s:%s (retried %s times)", host, port, retryCount), lastException);
    }
    if (toReturn == null) {
      throw new RemoteException("Bad state: client socket is null");
    }
    log.debug("Connection to %s:%s succeeded", host, port);
    return toReturn; 
  }
  
  private Socket newSocketAsync(final String host, final int port) throws IOException {
    final AtomicReference<Socket> socketRef = new AtomicReference<Socket>(new Socket());
    Future<Void> result = Spawn.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        Socket socket = socketRef.get();
        socket.connect(new InetSocketAddress(host, port), connectionTimeout);
        log.debug("Connection established to %s:%s", host, port);
        return null;
      }
    });
    
    try {
      result.get(connectionTimeout, TimeUnit.MILLISECONDS);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }
    } catch (TimeoutException e) {
      socketRef.get().close();
      throw new IOException("Could not connect within specified timeout");
    } catch (InterruptedException e) {
      throw new ThreadInterruptedException();
    }
    
    // safety check
    if (!socketRef.get().isConnected() || !socketRef.get().isBound()) {
      IoUtils.closeSilently(socketRef.get());
      throw new IOException("Socket not bound/connected: closing");
    }
    return socketRef.get();
  }
}
