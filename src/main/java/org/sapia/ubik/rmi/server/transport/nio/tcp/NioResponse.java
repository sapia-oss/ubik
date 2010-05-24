package org.sapia.ubik.rmi.server.transport.nio.tcp;

import org.sapia.ubik.rmi.server.VmId;

/**
 * Encapsulates data necessary for marshalling the response properly.
 * 
 * @author yduchesne
 * @see NioResponseEncoder
 *
 */
public class NioResponse {
  
  private VmId associatedVmId;
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

}
