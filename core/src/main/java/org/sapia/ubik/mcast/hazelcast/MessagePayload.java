package org.sapia.ubik.mcast.hazelcast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.serialization.SerializationStreams;

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
    this(serialize(event));
  }

  public byte[] getPayload() {
    return payload;
  }

  public RemoteEvent getRemoteEvent() throws ClassNotFoundException, IOException {
    return deserialize(payload);
  }

  @Override
  public void readData(ObjectDataInput in) throws IOException {
    payload = (byte[]) in.readObject();
  }

  @Override
  public void writeData(ObjectDataOutput out) throws IOException {
    out.writeObject(payload);
  }

  // --------------------------------------------------------------------------

  private static byte[] serialize(Object input) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream os = SerializationStreams.createObjectOutputStream(bos);
    os.writeObject(input);
    os.flush();
    os.close();
    return bos.toByteArray();
  }

  private static RemoteEvent deserialize(byte[] input)  throws ClassNotFoundException, IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(input);
    ObjectInputStream is = SerializationStreams.createObjectInputStream(bis);
    try {
      return (RemoteEvent) is.readObject();
    } finally {
      is.close();
    }
  }
}
