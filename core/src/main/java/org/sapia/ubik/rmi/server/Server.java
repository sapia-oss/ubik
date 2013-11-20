package org.sapia.ubik.rmi.server;

import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;

/**
 * Specifies the behavior of Ubik RMI server implementations. Allows to
 * implement different types of servers.
 * 
 * @author Yanick Duchesne
 */
public interface Server {
  /**
   * Returns this instance's address.
   * 
   * a {@link ServerAddress}.
   */
  public ServerAddress getServerAddress();

  /***
   * Starts this server - this method should not block infinitely.
   * 
   * @throws RemoteException
   *           if a problem occurs while starting up.
   */
  public void start() throws RemoteException;

  /**
   * Closes this server, which cleanly shuts down.
   */
  public void close();
}
