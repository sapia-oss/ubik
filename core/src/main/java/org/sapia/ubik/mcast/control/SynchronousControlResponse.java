package org.sapia.ubik.mcast.control;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.net.ServerAddress;

/**
 * Base class for synchronous control responses.
 * 
 * @author yduchesne
 *
 */
public abstract class SynchronousControlResponse implements Externalizable {

  static final long serialVersionUID = 1L;

  private String        originNode;
  private ServerAddress originAddress;

  public SynchronousControlResponse() {
  }

  protected SynchronousControlResponse(String originNode, ServerAddress originAddress) {
    this.originNode    = originNode;
    this.originAddress = originAddress;
  }

  public String getOriginNode() {
    return originNode;
  }
  
  public ServerAddress getOriginAddress() {
    return originAddress;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    originNode    = (String) in.readUTF();
    originAddress = (ServerAddress) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(originNode);
    out.writeObject(originAddress);
  }

}
