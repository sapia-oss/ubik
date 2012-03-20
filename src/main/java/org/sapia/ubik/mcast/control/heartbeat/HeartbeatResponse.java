package org.sapia.ubik.mcast.control.heartbeat;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.mcast.control.ControlResponse;
import org.sapia.ubik.net.ServerAddress;

/**
 * An {@link HeartbeatResponse} is sent from a slave to the master, as a confirmation that 
 * is is alive.
 * 
 * @author yduchesne
 *
 */
public class HeartbeatResponse extends ControlResponse {
	
	private ServerAddress unicastAddress;

	/**
	 * Meant for externalization
	 */
	public HeartbeatResponse() {
	}
	
	/**
	 * @param requestId the identifier of the original {@link HeartbeatRequest} to which
	 * this response corresponds.
	 */
	public HeartbeatResponse(long requestId, ServerAddress addr) {
		super(requestId);
		this.unicastAddress = addr;
	}
	
	/**
	 * @return the unicast {@link ServerAddress} of the node that sent this response.
	 */
	public ServerAddress getUnicastAddress() {
	  return unicastAddress;
  }
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	  super.readExternal(in);
	  unicastAddress = (ServerAddress) in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	  super.writeExternal(out);
	  out.writeObject(unicastAddress);
	}

}
