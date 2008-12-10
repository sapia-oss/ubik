package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;


/**
 * Implements a factory of <code>SocketRmiConnection</code> instances.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketRmiConnectionFactory extends SocketConnectionFactory {
  
  private long _resetInterval;
  
  /**
   * Constructor for RMIConnectionFactory.
   */
  public SocketRmiConnectionFactory() {
    super();
  }
  
  /**
   * @see SocketRmiConnection#setResetInterval(long)
   */
  public SocketRmiConnectionFactory setResetInterval(long resetInterval){
    _resetInterval = resetInterval;
    return this;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(Socket)
   */
  public Connection newConnection(Socket sock) throws IOException {
    SocketRmiConnection conn = new SocketRmiConnection(sock, _loader);
    conn.setResetInterval(_resetInterval);
    return conn;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port)
    throws IOException {
    SocketRmiConnection conn = new SocketRmiConnection(new Socket(host, port),
        _loader);

    return conn;
  }
}
