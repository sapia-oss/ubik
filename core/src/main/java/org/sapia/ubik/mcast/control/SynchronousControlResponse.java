package org.sapia.ubik.mcast.control;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class SynchronousControlResponse implements Externalizable {

  static final long serialVersionUID = 1L;

  private String originNode;

  public SynchronousControlResponse() {
  }

  protected SynchronousControlResponse(String originNode) {
    this.originNode = originNode;
  }

  public String getOriginNode() {
    return originNode;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    originNode = (String) in.readUTF();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(originNode);
  }

}
