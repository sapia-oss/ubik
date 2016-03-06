package org.sapia.ubik.mcast.bou;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Base64;
import org.sapia.ubik.util.Serialization;

public class BroadcastOverUnicastMulticastAddress implements MulticastAddress, Externalizable {

  static final long serialVersionUID = 1L;

  public static final String TRANSPORT = "bou";

  private ServerAddress unicastAddress;
  
  /**
   * DO NOT CALL: meant for serialization only.
   */
  public BroadcastOverUnicastMulticastAddress() {
  }

  public BroadcastOverUnicastMulticastAddress(ServerAddress unicastAddress) {
    this.unicastAddress = unicastAddress;
  }

  @Override
  public String getTransportType() {
    return TRANSPORT;
  }

  public ServerAddress getUnicastAddress() {
    return unicastAddress;
  }

  @Override
  public int hashCode() {
    return unicastAddress.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BroadcastOverUnicastMulticastAddress) {
      BroadcastOverUnicastMulticastAddress other = (BroadcastOverUnicastMulticastAddress) obj;
      return other.unicastAddress.equals(unicastAddress);
    }
    return false;
  }

  @Override
  public Map<String, String> toParameters() {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_UNICAST);
      params.put(Consts.BROADCAST_MEMORY_NODE, Base64.encodeBytes(Serialization.serialize(unicastAddress)));
      return params;
    } catch (IOException e) {
      throw new IllegalStateException("Could not perform serialization", e);
    }
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    unicastAddress = (ServerAddress) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(unicastAddress);
  }

  public String toString() {
    return String.format("[%s]", unicastAddress);
  }  
}