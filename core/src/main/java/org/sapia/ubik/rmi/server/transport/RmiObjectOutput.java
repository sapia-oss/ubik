package org.sapia.ubik.rmi.server.transport;

import org.sapia.ubik.rmi.server.VmId;

public interface RmiObjectOutput {
  
  public void setUp(VmId id, String transportType);
  
}
