package org.sapia.ubik.net;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

/**
 * A default {@link RMIClientSocketFactory}.
 * 
 * @see SocketConnectionFactory
 * 
 * @author Yanick Duchesne
 */
public class DefaultRMIClientSocketFactory implements RMIClientSocketFactory {
  /**
   * @see java.rmi.server.RMIClientSocketFactory#createSocket(java.lang.String,
   *      int)
   */
  public Socket createSocket(String host, int port) throws IOException {
    return new Socket(host, port);
  }
}
