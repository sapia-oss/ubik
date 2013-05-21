package org.sapia.ubik.rmi.server.transport.netty;

import java.rmi.Remote;

public interface NettyService extends Remote {

  public String getMessage();
  
}
