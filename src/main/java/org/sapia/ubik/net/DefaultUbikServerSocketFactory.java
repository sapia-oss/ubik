package org.sapia.ubik.net;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DefaultUbikServerSocketFactory implements UbikServerSocketFactory {
  /**
   * @see org.sapia.ubik.net.UbikServerSocketFactory#createServerSocket(int, java.lang.String)
   */
  public ServerSocket createServerSocket(int port, String bindAddr)
    throws IOException {
    return new ServerSocket(port, 50, InetAddress.getByName(bindAddr));
  }

  /**
   * @see java.rmi.server.RMIServerSocketFactory#createServerSocket(int)
   */
  public ServerSocket createServerSocket(int port) throws IOException {
    return new ServerSocket(port);
  }
}
