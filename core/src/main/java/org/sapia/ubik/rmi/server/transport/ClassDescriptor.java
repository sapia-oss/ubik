package org.sapia.ubik.rmi.server.transport;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;


/**
 * Encapsulates a class name and creates a {@link Class} instance
 * using that name, from a given {@link ClassLoader}. Used to transport class
 * info over the wire, without incurring {@link ClassNotFoundException}s.
 *
 * @author Yanick Duchesne
 */
public class ClassDescriptor implements Externalizable {
  
  private static final long serialVersionUID = 6382603348796329430l;
  
  private static Map<String, Class<?>> primitives = new HashMap<String, Class<?>>();

  static {
    primitives.put(boolean.class.getName(), boolean.class);
    primitives.put(byte.class.getName(), byte.class);
    primitives.put(char.class.getName(), char.class);
    primitives.put(short.class.getName(), short.class);
    primitives.put(int.class.getName(), int.class);
    primitives.put(long.class.getName(), long.class);
    primitives.put(float.class.getName(), float.class);
    primitives.put(double.class.getName(), double.class);
  }

  private transient Class<?> type;
  private String          className;
  private boolean         primitive;

  /* Do not use; meant for deserialization. */
  public ClassDescriptor() {
  }

  public ClassDescriptor(Class<?> type) {
    this.type        = type;
    this.primitive   = type.isPrimitive();
    this.className   = type.getName();
  }

  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    className   = in.readUTF();
    primitive   = in.readBoolean();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(className);
    out.writeBoolean(primitive);
  }

  public Class<?> resolve(ClassLoader loader) throws ClassNotFoundException {
    if (primitive) {
      Class<?> clazz = (Class<?>) primitives.get(className);

      if (clazz == null) {
        throw new ClassNotFoundException(className);
      }

      return clazz;
    } else {
      if (type == null) {
        type = loader.loadClass(className);
      }
      return type;
    }
  }
}
