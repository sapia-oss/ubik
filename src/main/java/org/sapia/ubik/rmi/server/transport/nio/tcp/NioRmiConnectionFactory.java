package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;

/**
 * Implements a factory of {@link NioTcpRmiClientConnection} instances.
 */
public class NioRmiConnectionFactory extends SocketConnectionFactory {
  
  private int bufsize;
  
  public NioRmiConnectionFactory(int bufsize) {
  	super(NioTcpTransportProvider.TRANSPORT_TYPE);
    this.bufsize = bufsize;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(Socket)
   */
  public Connection newConnection(Socket sock) throws IOException {
    NioTcpRmiClientConnection conn = new NioTcpRmiClientConnection(sock, bufsize);
    return conn;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port) throws IOException {
    NioTcpRmiClientConnection conn = new NioTcpRmiClientConnection(new Socket(host, port), bufsize);

    return conn;
  }
}
