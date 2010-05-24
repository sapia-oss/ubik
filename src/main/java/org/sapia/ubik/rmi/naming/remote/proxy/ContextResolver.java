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
 *
 * @author yduchesne
 */
public interface ContextResolver {
  
  public RemoteContext resolve(ServerAddress addr) throws RemoteException;
  public RemoteContext resolve(String host, int port) throws RemoteException;
  
  
}
