package org.sapia.ubik.rmi.server.transport.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.util.Assertions;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 * Implements a {@link Server} that receives requests from Ubik RMI clients
 * over HTTP.
 * <p>
 * An instance of this class maintains a thread pool. The threads are used to process
 * incoming commands. Threads are released to the pool once their execution has completed.
 *
 * @see org.sapia.ubik.rmi.Consts#SERVER_MAX_THREADS
 *
 * @author Yanick Duchesne
 */
class HttpRmiServer implements Server, HttpConsts {

  private int           localPort;  
  private Router        handlers;
  private Connection    connection;
  private HttpAddress   address;
  
  /**
   * Creates an instance of this class.
   *
   * @param router the {@link Router} that holds the {@link Handler}s used to process incoming requests.
   * @param transportType a "transport type" identifier.
   * @param port a port (must be >= 0).
   */
  HttpRmiServer(
      Router router,
      Uri serverUrl,
      int localPort) {
    
    Assertions.illegalState(serverUrl.getPort() <= 0, "Server does not support dynamic port");
    this.address    = new HttpAddress(serverUrl);
    this.handlers   = router;
    this.localPort  = localPort;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#close()
   */
  public void close() {
    if (this.connection != null) {
      try {
        this.connection.close();
      } catch (Exception e) {
        // noop
      }
    }
    if (handlers != null) {
      handlers.shutdown();
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return this.address;
  }


  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() throws RemoteException {
    try {
      org.simpleframework.transport.Server server = new ContainerServer(handlers);
      Connection connection = new SocketConnection(server);
      SocketAddress address = new InetSocketAddress(localPort);
      connection.connect(address);
    } catch (UnknownHostException e) {
      throw new RemoteException("Could not acquire local address", e);
    } catch (IOException e) {
      throw new RemoteException("Could not instantiate server socket", e);
    } catch (Exception e) {
      throw new RemoteException("Could not instantiate server socket", e);
    }

  }
}
