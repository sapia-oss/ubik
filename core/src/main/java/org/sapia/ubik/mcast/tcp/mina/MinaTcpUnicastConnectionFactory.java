package org.sapia.ubik.mcast.tcp.mina;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;

/**
 * Implements a factory of {@link MinaTcpUnicastConnection} instances.
 */
public class MinaTcpUnicastConnectionFactory extends SocketConnectionFactory {

  private int bufsize;

  public MinaTcpUnicastConnectionFactory(int bufsize) {
    super(MinaTcpUnicastAddress.TRANSPORT_TYPE);
    this.bufsize = bufsize;
  }

  @Override
  public Connection newConnection(Socket sock) throws IOException {
    MinaTcpUnicastConnection conn = new MinaTcpUnicastConnection(sock, bufsize);
    return conn;
  }

  @Override
  public Connection newConnection(String host, int port) throws IOException {
    try {
      return new MinaTcpUnicastConnection(newSocket(host, port), bufsize);
    } catch (ConnectException e) {
      throw new RemoteException(String.format("Could not connect to %s:%s", host, port));
    } catch (SocketException e) {
      throw new RemoteException(String.format("Could not connect to %s:%s", host, port));
    }
  }
}
