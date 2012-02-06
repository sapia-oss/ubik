package org.sapia.ubik.rmi.server.transport;

import java.rmi.RemoteException;
import java.util.Properties;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Server;


/**
 * This interface abstracts the transport layer's implementation.
 *
 * @author Yanick Duchesne
 */
public interface TransportProvider {
  /**
   * Returns a client connection pool to the given server address.
   *
   * @param address a {@link ServerAddress}.
   * @return a {@link ConnectionOptionsException}.
   * @throws RemoteException if a problem occurs creating the connection.
   */
  public Connections getPoolFor(ServerAddress address)
    throws RemoteException;

  /**
   * Returns a server implementation.
   *
   * @param props the {@link Properties} used to create the server.
   * @return a {@link Server} instance.
   */
  public Server newServer(Properties props) throws RemoteException;

  /**
   * This method is called by Ubik RMI's runtime when a server is needed on
   * the client side to recieve asynchronous responses (i.e.: callbacks). Implementations
   * must in this case provide a "default" {@link Server} instance.
   *
   * @return a {@link Server}.
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
