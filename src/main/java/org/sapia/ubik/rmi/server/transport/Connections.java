package org.sapia.ubik.rmi.server.transport;

import java.rmi.RemoteException;


/**
 * Specifies the behavior of pools of client-side {@link RmiConnection}s.
 *
 * @author Yanick Duchesne
 */
public interface Connections {
  /**
   * Acquires a connection from this pool.
   *
   * @return a {@link RmiConnection}
   * @throws RemoteException if a problem occurs acquiring a connection.
   */
  public RmiConnection acquire() throws RemoteException;

  /**
   * Releases the given connection to this pool.
   *
   * @param conn a {@link RmiConnection}
   */
  public void release(RmiConnection conn);

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
