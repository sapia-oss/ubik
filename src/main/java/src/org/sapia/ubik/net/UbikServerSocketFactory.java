package org.sapia.ubik.net;

import java.io.IOException;

import java.net.ServerSocket;

import java.rmi.server.RMIServerSocketFactory;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface UbikServerSocketFactory extends RMIServerSocketFactory {
  /**
   * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException;

  /**
   * @param port the port on which the socket should listen.
   * @param bindAddr the local IP address to which the socket should be bound.
   * @return a <code>ServerSocket</code>.
   * @throws IOException if a problem occurs creating the socket.
   */
  public ServerSocket createServerSocket(int port, String bindAddr)
    throws IOException;
}
