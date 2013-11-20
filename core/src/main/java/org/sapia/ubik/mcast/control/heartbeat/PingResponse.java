package org.sapia.ubik.mcast.control.heartbeat;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.mcast.control.SynchronousControlResponse;

public class PingResponse extends SynchronousControlResponse {

  public PingResponse() {
  }

  public PingResponse(String originNode) {
    super(originNode);
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
  }

}
