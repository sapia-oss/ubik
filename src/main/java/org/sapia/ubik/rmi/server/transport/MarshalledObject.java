package org.sapia.ubik.rmi.server.transport;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.rmi.Utils;
import org.sapia.ubik.rmi.server.RmiUtils;
import org.sapia.ubik.rmi.server.VmId;


/**
 * An instance of this class keeps an object in byte form.
 *
 * @author Yanick Duchesne
 * 17-Sep-2003
 */
public class MarshalledObject implements Externalizable {
  private byte[] _bytes;
  private String _codebase;

  /**
   * Do not use. Meant for externalization.
   */
  public MarshalledObject() {
  }

  /**
   * Constructor for MarshalledObject.
   */
  public MarshalledObject(Object o, VmId id, String transportType, String codebase)
    throws IOException {
    if (o != null) {
      _bytes = serialize(id, transportType, o);
    }
    _codebase = codebase;
  }

  /**
   * Returns the object that this instance wraps. Internally uses the
   * given classloader to deserialize the wrapped object (kept as an array
   * of bytes).
   *
   * @param loader the ClassLoader to use for deserialization.
   * @return an <code>Object</code>
   * @throws IOException if an error occurs while deserializing.
   * @throws ClassNotFoundException if the class of the object to deserialize
   * was not found.
   */
  public Object get(ClassLoader loader)
    throws IOException, ClassNotFoundException {
    if (_bytes == null) {
      return _bytes;
    }
    if(_codebase != null && RmiUtils.CODE_DOWNLOAD){
      return Utils.deserialize(_bytes, loader, _codebase); 
    }
    else{
      return Utils.deserialize(_bytes, loader);
    }
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _bytes = (byte[]) in.readObject();
    _codebase = (String) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(_bytes);
    out.writeObject(_codebase);
  }

  private static byte[] serialize(VmId vmid, String transportType, Object o)
    throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    MarshalOutputStream   ous = new MarshalOutputStream(bos);
    ous.setUp(vmid, transportType);
    ous.writeObject(o);
    ous.flush();
    ous.close();

    return bos.toByteArray();
  }
}
