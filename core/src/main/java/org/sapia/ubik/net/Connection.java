package org.sapia.ubik.net;

import java.io.IOException;
import java.net.SocketTimeoutException;
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
   * @param o
   *          an {@link Object}.
   * @throws IOException of an  I/O error occurs.
   * @throws RemoteException if their was a problem connecting to the remote host.
   */
  public void send(Object o) throws IOException, RemoteException;

  /**
   * Receives an object from the server with which this connection communicates.
   * 
   * @return an {@link Object}.
   * @throws IOException of an  I/O error occurs.
   * @throws ClassNotFoundException if the class of the deserialized object is not found.
   * @throws RemoteException if their was a problem connecting to the remote host.
   */
  public Object receive() throws IOException, ClassNotFoundException, RemoteException;

  /**
   * Receives an object from the server with which this connection communicates.
   * 
   * @param a timeout, in milliseconds.
   * @return an {@link Object}.
   * @throws IOException of an  I/O error occurs.
   * @throws ClassNotFoundException if the class of the deserialized object is not found.
   * @throws RemoteException if their was a problem connecting to the remote host.
   * @throws SocketTimeoutException if the object was not received within the specified timeout.
   */
  public Object receive(long timeout) throws IOException, ClassNotFoundException, RemoteException, SocketTimeoutException;
  
  /**
   * Closes this connection.
   */
  public void close();

  /**
   * Returns "address" of the server with which this connection communicates.
   * 
   * @return a {@link ServerAddress}.
   */
  public ServerAddress getServerAddress();
}
