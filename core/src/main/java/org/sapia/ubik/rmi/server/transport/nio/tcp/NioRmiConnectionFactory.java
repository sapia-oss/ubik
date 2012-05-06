package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;

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
    return new NioTcpRmiClientConnection(sock, bufsize);
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port) throws IOException {
    try {
      return new NioTcpRmiClientConnection(new Socket(host, port), bufsize);
    } catch (ConnectException e) {
      throw new RemoteException(String.format("Could not connect to %s:%s", host, port));
    } catch (SocketException e) {
      throw new RemoteException(String.format("Could not connect to %s:%s", host, port));
    }        
  }
}
