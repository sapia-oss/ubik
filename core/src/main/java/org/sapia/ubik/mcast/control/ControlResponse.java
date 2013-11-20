package org.sapia.ubik.mcast.control;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The base class for control responses.
 * 
 * @author yduchesne
 * 
 */
public abstract class ControlResponse implements Externalizable {

  private long requestId;

  /** Meant for externalization */
  public ControlResponse() {
  }

  protected ControlResponse(long requestId) {
    this.requestId = requestId;
  }

  /**
   * @return the ID of this instance's corresponding request.
   */
  public long getRequestId() {
    return requestId;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    requestId = in.readLong();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(requestId);
  }
}
