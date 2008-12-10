package org.sapia.ubik.net;

import java.io.IOException;

import java.net.Socket;

import java.rmi.server.RMIClientSocketFactory;


/**
 * Implements a factory of <code>SocketConnection</code> instances.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketConnectionFactory implements ConnectionFactory {
  protected ClassLoader            _loader;
  protected RMIClientSocketFactory _clientSocketFactory;

  public SocketConnectionFactory() {
    this(Thread.currentThread().getContextClassLoader());
  }

  public SocketConnectionFactory(RMIClientSocketFactory client) {
    this(Thread.currentThread().getContextClassLoader());
    _clientSocketFactory = client;
  }

  public SocketConnectionFactory(RMIClientSocketFactory client,
    ClassLoader loader) {
    this(loader);
    _clientSocketFactory = client;
  }

  public SocketConnectionFactory(ClassLoader loader) {
    _loader = loader;
  }

  /**
   * @see org.sapia.ubik.net.ConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port)
    throws IOException {
    if (_clientSocketFactory == null) {
      return new SocketConnection(new Socket(host, port), _loader);
    } else {
      return new SocketConnection(_clientSocketFactory.createSocket(host, port),
        _loader);
    }
  }

  /**
   * Creates a new Connection around the given socket.
   *
   * @see org.sapia.ubik.net.ConnectionFactory#newConnection(String, int)
   * @return a <code>SocketConnection</code>.
   */
  public Connection newConnection(Socket sock) throws IOException {
    return new SocketConnection(sock, _loader);
  }
}
