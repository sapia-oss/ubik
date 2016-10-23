package org.sapia.ubik.mcast.tcp.mina;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;

/**
 * Implements a factory of {@link MinaTcpUnicastConnection} instances.
 */
public class MinaTcpUnicastConnectionFactory extends SocketConnectionFactory {

  public MinaTcpUnicastConnectionFactory(int bufsize) {
    super(MinaTcpUnicastAddress.TRANSPORT_TYPE);
  }

  @Override
  public Connection newConnection(Socket sock) throws IOException {
    MinaTcpUnicastConnection conn = new MinaTcpUnicastConnection(sock, bufsize);
    return conn;
  }

  @Override
  public Connection newConnection(String host, int port) throws IOException {
    return new MinaTcpUnicastConnection(newSocket(host, port), bufsize);
  }
}
