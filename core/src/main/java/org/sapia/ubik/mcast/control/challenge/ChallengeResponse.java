package org.sapia.ubik.mcast.control.challenge;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.mcast.control.ControlResponse;
import org.sapia.ubik.net.ServerAddress;

public class ChallengeResponse extends ControlResponse {
  
  static final long serialVersionUID = 1L;

  public enum Code {
    ACCEPTED, DENIED;
  }

  private ServerAddress address;
  private Code code;

  /** meant of externalization only */
  public ChallengeResponse() {
  }

  public ChallengeResponse(long requestId, Code code, ServerAddress address) {
    super(requestId);
    this.code = code;
    this.address = address;
  }

  /**
   * @return the {@link Code} indicating if the challenge succeeded or not.
   */
  public Code getCode() {
    return code;
  }

  /**
   * @return the unicast {@link ServerAddress} of the node that sent the
   *         response.
   */
  public ServerAddress getUnicastAddress() {
    return address;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    this.code = (Code) in.readObject();
    this.address = (ServerAddress) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(code);
    out.writeObject(address);
  }

}
