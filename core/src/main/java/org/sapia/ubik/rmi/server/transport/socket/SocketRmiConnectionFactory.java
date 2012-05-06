package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.SocketConnectionFactory;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Props;


/**
 * Implements a factory of {@link SocketRmiConnection} instances.
 *
 * @author Yanick Duchesne
 */
public class SocketRmiConnectionFactory extends SocketConnectionFactory {
	
	private int bufsize = Props.getSystemProperties().getIntProperty(
													Consts.MARSHALLING_BUFSIZE, 
													Consts.DEFAULT_MARSHALLING_BUFSIZE
												);
  
  private long resetInterval;
  
  public SocketRmiConnectionFactory(String transportType) {
    super(transportType);
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
    SocketRmiConnection conn = new SocketRmiConnection(transportType, sock, loader, bufsize);
    conn.setResetInterval(resetInterval);
    return conn;
  }

  /**
   * @see org.sapia.ubik.net.SocketConnectionFactory#newConnection(String, int)
   */
  public Connection newConnection(String host, int port)
    throws IOException {
    try {
      return new SocketRmiConnection(transportType, new Socket(host, port), loader, bufsize);
    } catch (ConnectException e) {
      throw new RemoteException(String.format("Could not connect to %s:%s", host, port));
    } catch (SocketException e) {
      throw new RemoteException(String.format("Could not connect to %s:%s", host, port));
    }        
  }
}
