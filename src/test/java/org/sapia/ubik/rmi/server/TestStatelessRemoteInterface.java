package org.sapia.ubik.rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TestStatelessRemoteInterface extends Remote, Stateless {
  
  public void perform() throws RemoteException;

}
