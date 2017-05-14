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
public class DefaultRMIClientSocketFactory implements RMIClientSocketFactoryExt {

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return new Socket(host, port);
  }
  
  @Override
  public Socket createSocket() {
    return new Socket();
  }
}
