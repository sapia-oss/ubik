package org.sapia.ubik.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.sapia.ubik.concurrent.ThreadStartup;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;


/**
 * Implements a socket server.
 *
 * @author Yanick Duchesne
 */
public abstract class SocketServer implements Runnable {
  
  private static final long STARTUP_DELAY = 10000;
  
  protected Category                  log = Log.createCategory(getClass());
  private   ServerSocket              server;
  protected SocketConnectionFactory   fac;
  private   String                    address;
  private   ThreadPool<Request>       tp;
  private   ThreadStartup             startupBarrier = new ThreadStartup();

  /**
   * @param port the port that this server should listen on.
   * @param tp the {@link ThreadPool} to use to create worker threads for request processing.
   * @param socketFactory the {@link UbikServerSocketFactory} that will be used to create this instance's
   * {@link ServerSocket}.
   * @throws java.io.IOException if an IO problem occurs construction this instance.
   */
  protected SocketServer(
      int port, 
      ThreadPool<Request> tp,
      UbikServerSocketFactory socketFactory) throws java.io.IOException {
    this(port, new SocketConnectionFactory(), tp, socketFactory);
  }

  /**
   * @param bindAddr the address to which the server should be bound.
   * @param port the port that this server should listen on.
   * @param tp the {@link ThreadPool} to use to create worker threads for request processing.
   * @param socketFactory the {@link UbikServerSocketFactory} that will be used to create this instance's
   * {@link ServerSocket}.
   * @throws java.io.IOException if an IO problem occurs construction this instance.
   */
  protected SocketServer(
      String bindAddr, 
      int port, 
      ThreadPool<Request> tp,
      UbikServerSocketFactory socketFactory) throws java.io.IOException {
    this(bindAddr, port, new SocketConnectionFactory(), tp, socketFactory);
  }

  /**
   * @param port the port that this server should listen on.
   * @param fac the {@link SocketConnectionFactory} for which to create server-side {@link Connection} instances.
   * @param tp the {@link ThreadPool} to use to create worker threads for request processing.
   * @param socketFactory the {@link UbikServerSocketFactory} that will be used to create this instance's
   * {@link ServerSocket}.
   * @throws java.io.IOException if an IO problem occurs construction this instance.
   */
  protected SocketServer(
      int port, 
      SocketConnectionFactory fac, 
      ThreadPool<Request> tp,
      UbikServerSocketFactory socketFactory) throws java.io.IOException {
    server   = socketFactory.createServerSocket(port);
    this.fac      = fac;
    this.tp       = tp;
  }

  /**
   * @param bindAddr the address to which the server should be bound.
   * @param port the port that this server should listen on.
   * @param fac the {@link SocketConnectionFactory} for which to create server-side {@link Connection} instances.
   * @param tp the {@link ThreadPool} to use to create worker threads for request processing.
   * @param socketFactory the {@link UbikServerSocketFactory} that will be used to create this instance's
   * {@link ServerSocket}.
   * @throws java.io.IOException if an IO problem occurs construction this instance.
   */
  protected SocketServer(
      String bindAddr, 
      int port,
      SocketConnectionFactory fac, 
      ThreadPool<Request> tp,
      UbikServerSocketFactory socketFactory) throws java.io.IOException {
    this.server = socketFactory.createServerSocket(port, bindAddr);
    this.fac    = fac;
    this.tp     = tp;
  }

  /**
   * Creates a new SocketServer instance
   *
   * @param tp
   * @param server
   * @throws IOException
   */
  protected SocketServer(ThreadPool<Request> tp, ServerSocket server)
    throws IOException {
    this(new SocketConnectionFactory(), tp, server);
  }

  /**
   * Creates a new SocketServer instance.
   *
   * @param fac
   * @param tp
   * @param server
   * @throws IOException
   */
  protected SocketServer(
      SocketConnectionFactory fac, 
      ThreadPool<Request> tp,
      ServerSocket server) throws IOException {
    this.server = server;
    this.fac    = fac;
    this.tp     = tp;
  }

  public String getAddress() {
    if (address == null) {
      address = server.getInetAddress().getHostAddress();

      if (address.equals("0.0.0.0")) {
        try {
          address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
          throw new IllegalStateException(e.getClass().getName() +
            " caught - msg " + e.getMessage());
        }
      }
    }

    return address;
  }

  public int getPort() {
    return server.getLocalPort();
  }
  
  public int getThreadCount(){
    return tp.getThreadCount();
  }

  public void close() {
    try {
      tp.shutdown();
      server.close();
    } catch (Throwable t) {
      log.error("Error caught while closing server", t);
    }
  }

  public void run() {
    try {
      tp.fill(1);
    } catch (Throwable t) {
      log.error("Error while trying to start server; aborting", t);
      return;
    }

    while (true) {
      try {
        Socket client = null;

        try {
          if (!startupBarrier.isStarted()) {
            try {
              server.setSoTimeout(1);
              client = server.accept();
            } catch (SocketTimeoutException e) {
              //noop;
            }

            server.setSoTimeout(0);
            startupBarrier.started();

            if (client == null) {
              continue;
            }
          } else {
            client = server.accept();
          }
        } catch (SocketTimeoutException ste) {
          //noop;
          continue;
        } catch (SocketException e) {
          if (!startupBarrier.isStarted()) {
            log.error("Error accepting client connections", e);            
            startupBarrier.failed(e);
          } else {
            log.info("Shutting down");
          }

          break;
        }

        final Connection conn = fac.newConnection(client);

        Request req = new Request(conn, new TCPAddress(getAddress(), getPort()));
        tp.acquire().exec(req);
      } catch (Throwable t) {
        if (handleError(t)) {
          close();

          break;
        }
      }
    }
  }

  /**
   * Call to block on this instance until its internal socket server
   * has been started.
   *
   * @throws InterruptedException if the calling thread is interrupted while waiting.
   * @throws SocketException if a socket-related exception occurs while starting up.
   * @throws Exception if a generic exception occurs while starting up.
   */
  public synchronized void waitStarted()
    throws InterruptedException, SocketException, Exception {
    startupBarrier.await(STARTUP_DELAY);
  }

  protected boolean handleError(Throwable t) {
    log.error("Error while handling request", t);
    return true;
  }
}
