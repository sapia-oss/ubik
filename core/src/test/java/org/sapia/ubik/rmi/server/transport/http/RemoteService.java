package org.sapia.ubik.rmi.server.transport.http;

import java.rmi.Remote;
import java.util.Iterator;

public interface RemoteService extends Remote {

  public Iterator<Integer> iterate();
  
}
