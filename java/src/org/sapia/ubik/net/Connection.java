package org.sapia.ubik.net;

import java.io.*;

import java.rmi.RemoteException;


/**
 * Specifies "connection" behavior: in this case, connections that send and
 * receive objects over the wire.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Connection {
  /**
   * Sends the given object to the server with which this connection
   * communicates.
   *
   * @param an <code>Object</code>.
   */
  public void send(Object o) throws IOException, RemoteException;

  /**
   * Receives an object from the server with which this connection
   * communicates.
   *
   * @return an <code>Object</code>.
   */
  public Object receive()
    throws IOException, ClassNotFoundException, RemoteException;

  /**
   * Closes this connection.
   */
  public void close();

  /**
   * Returns "address" of the server with which this connection
   * communicates.
   *
   * @return a <code>ServerAddress</code>.
   */
  public ServerAddress getServerAddress();
}
