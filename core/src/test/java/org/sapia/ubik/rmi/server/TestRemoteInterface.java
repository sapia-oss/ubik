package org.sapia.ubik.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TestRemoteInterface extends Remote {
  
  public void perform() throws RemoteException;

}
