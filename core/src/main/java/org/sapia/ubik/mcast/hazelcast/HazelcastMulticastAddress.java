package org.sapia.ubik.mcast.hazelcast;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.rmi.Consts;

/**
 * A Hazelcast-specific {@link MulticastAddress} implementation.
 *
 * @author yduchesne
 *
 */
public class HazelcastMulticastAddress implements MulticastAddress, Externalizable {

  static final long serialVersionUID = 1L;

  public static final String TRANSPORT = "hazelcast";

  private String topicName;

  /**
   * Default ctor. MEANT FOR SERIALIZATION ONLY.
   */
  public HazelcastMulticastAddress() {
  }

  /**
   * @param topicName the name of the topic to which this instance corresponds.
   */
  public HazelcastMulticastAddress(String topicName) {
    this.topicName = topicName;
  }

  @Override
  public int hashCode() {
    return topicName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof HazelcastMulticastAddress) {
      HazelcastMulticastAddress other = (HazelcastMulticastAddress) obj;
      return other.topicName.equals(topicName);
    }
    return false;
  }

  @Override
  public String getTransportType() {
    return TRANSPORT;
  }

  @Override
  public Map<String, String> toParameters() {
    Map<String, String> params = new HashMap<String, String>();
    params.put(Consts.BROADCAST_PROVIDER, Consts.BROADCAST_PROVIDER_HAZELCAST);
    params.put(Consts.BROADCAST_HAZELCAST_TOPIC, topicName);
    return params;
  }

  // --------------------------------------------------------------------------
  // Externalizable

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    topicName = in.readUTF();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(topicName);
  }

}
