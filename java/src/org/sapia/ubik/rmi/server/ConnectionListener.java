package org.sapia.ubik.rmi.server;

import org.sapia.ubik.net.ServerAddress;


/**
 * This interface defines callbacks pertaining to remote host lifecycle.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ConnectionListener {
  /**
   * This method is called when a remote host has closed its connection to the server.
   *
   * @param addr the <code>ServerAddress</code> of the server whose connection has been closed.
   */
  public void onConnectionClosed(ServerAddress addr);

  /**
   * This method is called when a remote host opens its connection to the server.
   *
   * @param addr the <code>ServerAddress</code> of the server whose connection has just been opened.
   */
  public void onConnectionCreated(ServerAddress addr);
}
