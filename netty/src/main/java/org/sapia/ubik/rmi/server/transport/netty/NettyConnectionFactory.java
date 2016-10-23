package org.sapia.ubik.rmi.server.transport.netty;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;

/**
 * Implements a factory of {@link NettyRmiClientConnection} instances.
 */
public class NettyConnectionFactory extends SocketConnectionFactory {

  public NettyConnectionFactory(int bufsize) {
    super(NettyTransportProvider.TRANSPORT_TYPE);
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(Socket)
   */
  public Connection newConnection(Socket sock) throws IOException {
    return new NettyRmiClientConnection(sock, bufsize);
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port) throws IOException {
    return new NettyRmiClientConnection(newSocket(host, port), bufsize);
  }
}
