package org.sapia.ubik.rmi.server.transport.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.Server;

import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;


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
  
  private String        path;
  private Uri           serverUrl;
  private ServiceMapper services;
  private ServerSocket  server;
  private HttpAddress   address;
  private int           maxThreads = 0;
  private int           localPort;

  /**
   * Creates an instance of this class.
   *
   * @param services a {@link ServiceMapper}.
   * @param transportType a "transport type" identifier.
   * @param path the URL path to which this server will correspond.
   * @param port a port (must be >= 0).
   */
  HttpRmiServer(ServiceMapper services, Uri serverUrl,
    String path, int localPort) {
    if (serverUrl.getPort() <= 0) {
      throw new IllegalStateException("Server does not support dynamic port");
    }
    this.serverUrl       = serverUrl;
    this.path            = path;
    this.services        = services;
    this.localPort       = localPort;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#close()
   */
  public void close() {
    if (this.server != null) {
      try {
        this.server.close();
      } catch (Exception e) {
        // noop
      }
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return this.address;
  }

  /**
   * This method should be called before the <code>start()</code> method.
   *
   * @param max the maximum number of threads that this instance will create.
   */
  void setMaxThreads(int max) {
    this.maxThreads = max;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() throws RemoteException {
    try {
      this.address = new HttpAddress(serverUrl);

      UbikHttpHandler svc = new UbikHttpHandler(serverUrl, maxThreads);
      services.addService(path, svc);
      
      Connection    conn = ConnectionFactory.getConnection(services);
      server = new ServerSocket(localPort);
      conn.connect(server);
    } catch (UnknownHostException e) {
      throw new RemoteException("Could not acquire local address", e);
    } catch (IOException e) {
      throw new RemoteException("Could not instantiate server socket", e);
    } catch (Exception e) {
      throw new RemoteException("Could not instantiate server socket", e);
    }

  }
}
