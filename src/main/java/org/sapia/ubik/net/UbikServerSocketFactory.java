package org.sapia.ubik.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;


/**
 * Specifies a factory of {@link ServerSocket}s.
 * 
 * @author Yanick Duchesne
 */
public interface UbikServerSocketFactory extends RMIServerSocketFactory {

  /**
   * @param port the port on which the socket should listen.
   * @param bindAddr the local IP address to which the socket should be bound.
   * @return a <code>ServerSocket</code>.
   * @throws IOException if a problem occurs creating the socket.
   */
  public ServerSocket createServerSocket(int port, String bindAddr)
    throws IOException;
}
