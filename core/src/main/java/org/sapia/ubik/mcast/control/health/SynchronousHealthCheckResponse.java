package org.sapia.ubik.mcast.control.health;

import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;

public class SynchronousHealthCheckResponse extends SynchronousControlResponse {
  
  public SynchronousHealthCheckResponse() {
  }
  
  public SynchronousHealthCheckResponse(String originNode, ServerAddress originAddress) {
    super(originNode, originAddress);
  }

}
