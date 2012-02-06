/*
 * ContextResolver.java
 *
 * Created on August 17, 2005, 3:25 PM
 */

package org.sapia.ubik.rmi.naming.remote.proxy;

import java.rmi.RemoteException;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;

/**
 * Resolves a {@link RemoteContext} context, given connection parameters.
 * 
 * @author yduchesne
 */
public interface ContextResolver {
  
  /**
   * @param addr the {@link ServerAddress} corresponding to the address of the remote JNDI server.
   * @return a {@link RemoteContext}. 
   * 
   * @throws RemoteException if no connection could be made.
   */
  public RemoteContext resolve(ServerAddress addr) throws RemoteException;

  /**
   * @param host the host corresponding to the remote JNDI server.
   * @param port the remote JNDI server port.
   * 
   * @return a {@link RemoteContext}. 
   * 
   * @throws RemoteException if no connection could be made.
   */  
  public RemoteContext resolve(String host, int port) throws RemoteException;
  
  
}
