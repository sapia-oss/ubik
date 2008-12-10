package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;

/**
 * Implements a factory of <code>SocketRmiConnection</code> instances.
 * 
 * @author Yanick Duchesne
 *         <dl>
 *         <dt><b>Copyright: </b>
 *         <dd>Copyright &#169; 2002-2003 <a
 *         href="http://www.sapia-oss.org">Sapia Open Source Software </a>. All
 *         Rights Reserved.</dd>
 *         </dt>
 *         <dt><b>License: </b>
 *         <dd>Read the license.txt file of the jar or visit the <a
 *         href="http://www.sapia-oss.org/license.html">license page </a> at the
 *         Sapia OSS web site</dd>
 *         </dt>
 *         </dl>
 */
public class NioRmiConnectionFactory extends SocketConnectionFactory {
  
  private int _bufsize;
  /**
   * Constructor for RMIConnectionFactory.
   */
  public NioRmiConnectionFactory(int bufsize) {
    _bufsize = bufsize;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(Socket)
   */
  public Connection newConnection(Socket sock) throws IOException {
    NioTcpRmiClientConnection conn = new NioTcpRmiClientConnection(sock, _bufsize);

    return conn;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port) throws IOException {
    NioTcpRmiClientConnection conn = new NioTcpRmiClientConnection(new Socket(
        host, port), _bufsize);

    return conn;
  }
}
