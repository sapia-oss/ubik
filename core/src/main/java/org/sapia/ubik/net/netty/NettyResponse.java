package org.sapia.ubik.net.netty;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.rmi.server.transport.netty.NettyRmiMessageEncoder;
import org.sapia.ubik.util.Strings;

/**
 * Encapsulates request or response data, depending on the context.
 * 
 * @author yduchesne
 * 
 * @see NettyRmiMessageEncoder
 *
 */
public class NettyResponse implements Externalizable {
  
  public static final int BUFSZ = 256;
  public static final int PREFIX_LEN = 4;
  public static final int PREFIX_CONTENT_LENGHT_BYTE_INDEX = 4;
  
  private VmId   associatedVmId;
  private Object object;
  private String transportType;
  private boolean error;

  /**
   * Do not use: meant for externalization only.
   */
  public NettyResponse() {
  }
  
  /**
   * @param object the {@link Object} consisting of this instance's payload.
   */
  public NettyResponse(Object object) {
    this(null, null, object);
  }

  
  /**
   * @param associatedVmId the {@link VmId} corresponding to the JVM from which this message originate.
   * @param transportType the transport type identifier.
   * @param object the {@link Object} consisting of this instance's payload.
   */
  public NettyResponse(VmId associatedVmId, String transportType, Object object) {
    this.associatedVmId = associatedVmId;
    this.transportType  = transportType;
    this.object         = object;
  }
  
  /**
   * Sets this instance's error flag to <code>true</code>.
   * @return this instance. 
   */
  NettyResponse error() {
    this.error = true;
    return this;
  }
  
  /**
   * @return <code>true</code> if this instance correspond to an error.
   */
  public boolean isError() {
    return error;
  }

  /**
   * @return this instance's {@link VmId}, identifying the JVM from which this instance
   * originates.
   */
  public VmId getAssociatedVmId() {
    return associatedVmId;
  }
  
  /**
   * @return the {@link Object} consisting of this instanc's payload.
   */
  public Object getObject() {
    return object;
  }

  /**
   * @return this instance's transport type, identifying the {@link TransportProvider} in the context
   * of which this instance is sent/received, or <code>null</code> if it was not specified at 
   * construction time.
   */
  public String getTransportType() {
    return transportType;
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
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "object", object);
  }

}
