/*
 * RemoteContextProvider.java
 */

package org.sapia.ubik.rmi.naming.remote;

import java.rmi.RemoteException;

/**
 * Defines the behavior of a provider of <code>RemoteContext</code>
 * instances.
 *
 * @author yduchesne
 */
public interface RemoteContextProvider extends java.rmi.Remote{
  
  /**
   * @return a <code>RemoteContext</code>, or <code>null</code> if
   * no such context exists.
   */
  public RemoteContext getRemoteContext() throws RemoteException;
}
