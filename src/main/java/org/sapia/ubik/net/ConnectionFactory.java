package org.sapia.ubik.net;

import java.io.IOException;


/**
 * This interface specifies the behavior of a factory of <code>Connection</code>
 * instances.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ConnectionFactory {
  /**
   * Returns a client <code>Connection</code> instance that connects to the
   * server at the given host and port.
   *
   * @param host the host to connect to.
   * @param port the port on which the server is listening at the given host.
   * @throws IOException if the connection instance could not be created.
   */
  public Connection newConnection(String host, int port)
    throws IOException;

  /**
   * Returns a <code>Connection</code> instance that should wrap the given
   * <code>Socket</code> instance. The latter can be a client-side socket, are
   * an instance that is returned by <code>ServerSocket.accept()</code>
   * method on the server-side.
   *
   * @param sock a <code>Socket</code> instance.
   * @throws IOException if the connection instance could not be created.
   */

  //  public Connection newConnection(java.net.Socket sock)
  //                           throws IOException, UnsupportedOperationException;
}
