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
 * Implements a <code>Server</code> that receives requests from Ubik RMI clients
 * over HTTP.
 * <p>
 * An instance of this class maintains a thread pool. The threads are used to process
 * incoming commands. Threads are released to the pool once their execution has completed.
 *
 * @see org.sapia.ubik.rmi.Consts#SERVER_MAX_THREADS
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class HttpRmiServer implements Server, HttpConsts {
  private String        _transportType;
  private String        _path;
  private Uri           _serverUrl;
  private ServiceMapper _services;
  private ServerSocket  _server;
  private HttpAddress   _address;
  private int           _maxThreads = 0;
  private int           _localPort;

  /**
   * Creates an instance of this class.
   *
   * @param services a <code>ServiceMapper</code>.
   * @param transportType a "transport type" identifier.
   * @param path the URL path to which this server will correspond.
   * @param port a port (must be >= 0).
   */
  HttpRmiServer(ServiceMapper services, String transportType, Uri serverUrl,
    String path, int localPort) {
    if (serverUrl.getPort() <= 0) {
      throw new IllegalStateException("Server does not support dynamic port");
    }

    _transportType   = transportType;
    _serverUrl       = serverUrl;
    _path            = path;
    _services        = services;
    _localPort       = localPort;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#close()
   */
  public void close() {
    if (_server != null) {
      try {
        _server.close();
      } catch (Exception e) {
        // noop
      }
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return _address;
  }

  /**
   * This method should be called before the <code>start()</code> method.
   *
   * @param max the maximum number of threads that this instance will create.
   */
  void setMaxThreads(int max) {
    _maxThreads = max;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() throws RemoteException {
    try {
      _address = new HttpAddress(_serverUrl);

      UbikHttpHandler svc = new UbikHttpHandler(_serverUrl,
          _services.getContext(), _maxThreads);
      _services.addService(_path, svc);

      HeaderHandler hh   = new HeaderHandler(_services);
      Connection    conn = ConnectionFactory.getConnection(_services);
      _server = new ServerSocket(_localPort);
      conn.connect(_server);
    } catch (UnknownHostException e) {
      throw new RemoteException("Could not acquire local address", e);
    } catch (IOException e) {
      throw new RemoteException("Could not instantiate server socket", e);
    }
  }
}
