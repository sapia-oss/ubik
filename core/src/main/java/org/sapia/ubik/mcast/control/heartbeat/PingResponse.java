package org.sapia.ubik.mcast.control.heartbeat;

import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;


/**
 * The response corresponding to {@link PingRequest}s.
 * 
 * @author yduchesne
 *
 */
public class PingResponse extends SynchronousControlResponse {
  
  static  final long serialVersionUID = 1L;

  public PingResponse() {
  }

  public PingResponse(String originNode, ServerAddress originAddress) {
    super(originNode, originAddress);
  }

}
