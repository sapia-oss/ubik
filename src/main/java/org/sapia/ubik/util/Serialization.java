package org.sapia.ubik.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;


/**
 * Provides serialization-related utility methods.
 * 
 * @author Yanick Duchesne
 */
public final class Serialization {

  private Serialization() {
  }
  
  /**
   * @param bytes the array of bytes corresponding to a serialized object.
   * @return the actual {@link Object} after deserialization.
   * @throws IOException if an IO problem occurs.
   * @throws ClassNotFoundException if a class in the deserialized object graph could 
   * not be found.
   */
  public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
    Object            o = ois.readObject();
    return o;
  }

  /**
   * Deserializes an object from the given bytes, using the provided {@link ClassLoader}.
   * 
   * @see #deserialize(byte[])
   */
  public static Object deserialize(byte[] bytes, ClassLoader loader) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStreamEx(new ByteArrayInputStream(bytes), loader);
    Object            o   = ois.readObject();
    return o;
  }

  /**
   * @param serializable the {@link Object} to serialize.
   * @return the array of bytes resulting from the serialization.
   * @throws IOException if an IO problem occurs while serializing.
   */
  public static byte[] serialize(Object serializable) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    ous = new ObjectOutputStream(bos);
    ous.writeObject(serializable);
    ous.flush();
    ous.close();
    
    return bos.toByteArray();
  }
  
  static final class ObjectInputStreamEx extends ObjectInputStream {
    private ClassLoader loader;
    
    ObjectInputStreamEx(InputStream is, ClassLoader loader)
    throws IOException {
      super(is);
      this.loader = loader;
    }
    
    /**
     * @see java.io.ObjectInputStream#resolveClass(ObjectStreamClass)
     */
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      if (loader != null) {
        return loader.loadClass(desc.getName());
      } else {
        return super.resolveClass(desc);
      }
    }
  }
}
