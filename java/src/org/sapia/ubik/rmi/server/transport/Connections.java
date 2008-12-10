package org.sapia.ubik.rmi.server.transport;

import java.rmi.RemoteException;

import org.sapia.ubik.net.Connection;


/**
 * Specifies the behavior of pools of client-side <code>Connection</code>.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Connections {
  /**
   * Acquires a connection from this pool.
   *
   * @return a <code>Connection</code>.
   * @throws RemoteException if a problem occurs acquiring a connection.
   */
  public RmiConnection acquire() throws RemoteException;

  /**
   * Releases the given connection to this pool.
   *
   * @param a <code>Connection</code>.
   */
  public void release(Connection conn);

  /**
   * Closes all connections kept internally and removes them.
   */
  public void clear();

  /**
   * Returns the "transport type" of the connections held by this instance.
   *
   * @return a transport type.
   */
  public String getTransportType();
}
