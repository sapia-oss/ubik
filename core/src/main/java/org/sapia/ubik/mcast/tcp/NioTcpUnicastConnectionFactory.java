package org.sapia.ubik.mcast.tcp;

import java.io.IOException;
import java.net.Socket;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;

/**
 * Implements a factory of {@link NioTcpUnicastConnection} instances.
 */
public class NioTcpUnicastConnectionFactory extends SocketConnectionFactory {
  
  private int bufsize;
  
  public NioTcpUnicastConnectionFactory(int bufsize) {
  	super(NioTcpUnicastDispatcher.TRANSPORT_TYPE);
    this.bufsize = bufsize;
  }

  @Override
  public Connection newConnection(Socket sock) throws IOException {
    NioTcpUnicastConnection conn = new NioTcpUnicastConnection(sock, bufsize);
    return conn;
  }

  @Override
  public Connection newConnection(String host, int port) throws IOException {
    return new NioTcpUnicastConnection(new Socket(host, port), bufsize);
  }
}
