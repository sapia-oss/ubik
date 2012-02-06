package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;


/**
 * Implements a factory of {@link SocketRmiConnection} instances.
 *
 * @author Yanick Duchesne
 */
public class SocketRmiConnectionFactory extends SocketConnectionFactory {
  
  private long resetInterval;
  
  public SocketRmiConnectionFactory() {
    super();
  }
  
  /**
   * @see SocketRmiConnection#setResetInterval(long)
   */
  public SocketRmiConnectionFactory setResetInterval(long resetInterval){
    this.resetInterval = resetInterval;
    return this;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(Socket)
   */
  public Connection newConnection(Socket sock) throws IOException {
    SocketRmiConnection conn = new SocketRmiConnection(sock, loader);
    conn.setResetInterval(resetInterval);
    return conn;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port)
    throws IOException {
    SocketRmiConnection conn = new SocketRmiConnection(new Socket(host, port), loader);
    return conn;
  }
}
