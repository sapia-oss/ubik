package org.sapia.ubik.mcast.hazelcast;

import java.io.IOException;

import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.util.Serialization;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

/**
 * Encapsulates an arbitrary payload, exchanged over an Hazelcast grid.
 * 
 * @author yduchesne
 *
 */
public class MessagePayload implements DataSerializable {

  private static final long serialVersionUID = 1L;
  
  private byte[] payload;

  public MessagePayload() {
  }
  
  MessagePayload(byte[] payload) {
    this.payload = payload;
  }
  
  MessagePayload(RemoteEvent event) throws IOException {
    this(Serialization.serialize(event));
  }
  
  public byte[] getPayload() {
    return payload;
  }
  
  public RemoteEvent getRemoteEvent() throws ClassNotFoundException, IOException {
    return (RemoteEvent) Serialization.deserialize(payload);
  }
  
  @Override
  public void readData(ObjectDataInput in) throws IOException {
    payload = (byte[]) in.readObject();
  }
  
  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeObject(payload);
  }
}
