package org.sapia.ubik.rmi.server.transport.mina;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.rmi.server.VmId;

/**
 * Encapsulates data necessary for marshalling the response properly.
 * 
 * @see NioResponseEncoder
 * 
 * @author yduchesne
 * 
 */
public class NioResponse implements Externalizable {
  
  private VmId   associatedVmId;
  private Object object;
  private String transportType;
  
  public VmId getAssociatedVmId() {
    return associatedVmId;
  }
  public void setAssociatedVmId(VmId associatedVmId) {
    this.associatedVmId = associatedVmId;
  }
  
  public Object getObject() {
    return object;
  }
  public void setObject(Object object) {
    this.object = object;
  }
  
  public String getTransportType() {
    return transportType;
  }
  public void setTransportType(String transportType) {
    this.transportType = transportType;
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    associatedVmId = (VmId) in.readObject();
    object         = in.readObject();
    transportType  = (String) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(associatedVmId);
    out.writeObject(object);
    out.writeObject(transportType);
  }

}
