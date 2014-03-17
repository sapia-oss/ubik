package org.sapia.ubik.rmi.server.transport;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Serialization;

/**
 * An instance of this class keeps an object in byte form.
 * 
 * @author Yanick Duchesne
 */
public class MarshalledObject implements Externalizable {

  private static int bufsize = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);

  private byte[] bytes;

  /**
   * Do not use. Meant for externalization.
   */
  public MarshalledObject() {
  }

  /**
   * Constructor for MarshalledObject.
   */
  public MarshalledObject(Object o, VmId id, String transportType) throws IOException {
    if (o != null) {
      bytes = serialize(id, transportType, o);
    }
  }

  /**
   * Returns the object that this instance wraps. Internally uses the given
   * classloader to deserialize the wrapped object (kept as an array of bytes).
   * 
   * @param loader
   *          the ClassLoader to use for deserialization.
   * @return an <code>Object</code>
   * @throws IOException
   *           if an error occurs while deserializing.
   * @throws ClassNotFoundException
   *           if the class of the object to deserialize was not found.
   */
  public Object get(ClassLoader loader) throws IOException, ClassNotFoundException {
    if (bytes == null) {
      return bytes;
    }
    return Serialization.deserialize(bytes, loader);
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    bytes = (byte[]) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(bytes);
  }

  private static byte[] serialize(VmId vmid, String transportType, Object o) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(bufsize);
    ObjectOutputStream ous = MarshalStreamFactory.createOutputStream(bos);
    ((RmiObjectOutput) ous).setUp(vmid, transportType);
    ous.writeObject(o);
    ous.flush();
    ous.close();
    return bos.toByteArray();
  }
}
