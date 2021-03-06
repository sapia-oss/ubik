package org.sapia.ubik.rmi.server.transport.mina;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;

/**
 * Implements a factory of {@link MinaRmiClientConnection} instances.
 */
public class MinaRmiConnectionFactory extends SocketConnectionFactory {

  private int bufsize;

  public MinaRmiConnectionFactory(int bufsize) {
    super(MinaTransportProvider.TRANSPORT_TYPE);
    this.bufsize = bufsize;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(Socket)
   */
  public Connection newConnection(Socket sock) throws IOException {
    return new MinaRmiClientConnection(sock, bufsize);
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port) throws IOException {
      return new MinaRmiClientConnection(newSocket(host, port), bufsize);
  }
}
