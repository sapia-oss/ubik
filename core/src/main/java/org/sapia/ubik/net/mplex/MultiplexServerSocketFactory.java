package org.sapia.ubik.net.mplex;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.sapia.ubik.net.UbikServerSocketFactory;

/**
 * Creates {@link MultiplexServerSocket}s.
 * 
 * @author Yanick Duchesne
 */
public class MultiplexServerSocketFactory implements UbikServerSocketFactory {
  /**
   * @see UbikServerSocketFactory#createServerSocket(int, java.lang.String)
   */
  public ServerSocket createServerSocket(int port, String bindAddr) throws IOException {
    return new MultiplexServerSocket(port, 50, InetAddress.getByName(bindAddr));
  }

  /**
   * @see UbikServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException {
    return new MultiplexServerSocket(port);
  }
}
