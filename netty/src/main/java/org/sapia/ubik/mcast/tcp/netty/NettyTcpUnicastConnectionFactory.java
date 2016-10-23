package org.sapia.ubik.mcast.tcp.netty;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;

/**
 * Implements a factory of {@link NettyTcpUnicastConnection} instances.
 */
public class NettyTcpUnicastConnectionFactory extends SocketConnectionFactory {

  public NettyTcpUnicastConnectionFactory(int bufsize) {
    super(NettyTcpUnicastAddress.TRANSPORT_TYPE);
  }

  @Override
  public Connection newConnection(Socket sock) throws IOException {
    NettyTcpUnicastConnection conn = new NettyTcpUnicastConnection(sock, bufsize);
    return conn;
  }

  @Override
  public Connection newConnection(String host, int port) throws IOException {
    return new NettyTcpUnicastConnection(newSocket(host, port), bufsize);
  }
}
