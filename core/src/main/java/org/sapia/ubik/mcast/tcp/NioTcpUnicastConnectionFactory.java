package org.sapia.ubik.mcast.tcp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;

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
    try {
      return new NioTcpUnicastConnection(new Socket(host, port), bufsize);
    } catch (ConnectException e) {
      throw new RemoteException(String.format("Could not connect to %s:%s", host, port));
    } catch (SocketException e) {
      throw new RemoteException(String.format("Could not connect to %s:%s", host, port));
    }        
  }
}
