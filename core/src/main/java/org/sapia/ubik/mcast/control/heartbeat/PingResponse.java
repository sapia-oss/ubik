package org.sapia.ubik.mcast.control.heartbeat;

import org.sapia.ubik.mcast.control.SynchronousControlResponse;


public class PingResponse extends SynchronousControlResponse {

	public PingResponse() {
	}
	
	public PingResponse(String originNode) {
		super(originNode);
	}
	

}
