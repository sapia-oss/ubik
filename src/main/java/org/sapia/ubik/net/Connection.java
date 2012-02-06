package org.sapia.ubik.net;

import java.io.IOException;
import java.rmi.RemoteException;


/**
 * Specifies "connection" behavior: in this case, connections that send and
 * receive objects over the wire.
 *
 * @author Yanick Duchesne
 */
public interface Connection {
  /**
   * Sends the given object to the server with which this connection
   * communicates.
   *
   * @param o an {@link Object}.
   */
  public void send(Object o) throws IOException, RemoteException;

  /**
   * Receives an object from the server with which this connection
   * communicates.
   *
   * @return an {@link Object}.
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
   * @return a {@link ServerAddress}.
   */
  public ServerAddress getServerAddress();
}
