package org.sapia.ubik.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;


/**
 * A default {@link UbikServerSocketFactory}, creating plain {@link ServerSocket}s.
 * 
 * @author Yanick Duchesne
 */
public class DefaultUbikServerSocketFactory implements UbikServerSocketFactory {
  
  /**
   * @see UbikServerSocketFactory#createServerSocket(int, java.lang.String)
   */
  public ServerSocket createServerSocket(int port, String bindAddr)
    throws IOException {
    return new ServerSocket(port, 50, InetAddress.getByName(bindAddr));
  }

  /**
   * @see UbikServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException {
    return new ServerSocket(port);
  }
}
