package org.sapia.ubik.rmi.server.transport;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Server;


/**
 * This interface abstracts the transport layer's implementation.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface TransportProvider {
  /**
   * Returns a client connection pool to the given server address.
   *
   * @param address a <code>ServerAddress</code>.
   * @return a <code>Connection</code>.
   * @throws RemoteException if a problem occurs creating the connection.
   */
  public Connections getPoolFor(ServerAddress address)
    throws RemoteException;

  /**
   * Releases the given client connection.
   *
   * @param a <code>Connection</code>.
   * @throws RemoteException if a problem occurs releasing the given connection.
   */

  //public void release(Connection conn) throws RemoteException;

  /**
   * Returns a server implementation.
   *
   * @param props the <code>Properties</code> used to create the server.
   * @return a <code>Server</code> instance.
   */
  public Server newServer(Properties props) throws RemoteException;

  /**
   * This method is called by Ubik RMI's runtime when a server is needed on
   * the client side to recieve asynchronous responses (i.e.: callbacks). Implementations
   * must in this case provide a "default" <code>Server</code> instance.
   *
   * @return a <code>Server</code>.
   */
  public Server newDefaultServer() throws RemoteException;

  /**
   * Returns this instance's "transport type".
   *
   * @return a transport type.
   */
  public String getTransportType();

  /**
   * Shuts down this provider.
   */
  public void shutdown();
}
