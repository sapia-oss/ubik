package org.sapia.ubik.rmi.server;

import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;


/**
 * Specifies the behavior of Ubik RMI server implementations.
 * Allows to implement different types of servers.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Server {
  /**
   * Returns this instance's address.
   *
   * a <code>ServerAddress</code>.
   */
  public ServerAddress getServerAddress();

  /***
   * Starts this server - this method should not block infinitely.
   *
   * @throws RemoteException if a problem occurs while starting up.
   */
  public void start() throws RemoteException;

  /**
   * Closes this server, which cleanly shuts down.
   */
  public void close();
}
