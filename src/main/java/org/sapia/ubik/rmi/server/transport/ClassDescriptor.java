package org.sapia.ubik.rmi.server.transport;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;


/**
 * Encapsulates a class name and creates a <code>Class</code> instance
 * using that name, from a given ClassLoader. Used to transport class
 * info over the wire, without incurring <code>ClassNotFoundException</code>s.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
@SuppressWarnings(value="unchecked")
public class ClassDescriptor implements Externalizable {
  private static final long serialVersionUID = 6382603348796329430l;
  private static Map _primitives = new HashMap();

  static {
    _primitives.put(boolean.class.getName(), boolean.class);
    _primitives.put(byte.class.getName(), byte.class);
    _primitives.put(char.class.getName(), char.class);
    _primitives.put(short.class.getName(), short.class);
    _primitives.put(int.class.getName(), int.class);
    _primitives.put(long.class.getName(), long.class);
    _primitives.put(float.class.getName(), float.class);
    _primitives.put(double.class.getName(), double.class);
  }

  private transient Class _type;
  private String          _className;
  private boolean         _primitive;

  /* Do not use; meant for deserialization. */
  public ClassDescriptor() {
  }

  public ClassDescriptor(Class type) {
    _type        = type;
    _primitive   = type.isPrimitive();
    _className   = type.getName();
  }

  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _className   = in.readUTF();
    _primitive   = in.readBoolean();
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(_className);
    out.writeBoolean(_primitive);
  }

  public Class resolve(ClassLoader loader) throws ClassNotFoundException {
    if (_primitive) {
      Class clazz = (Class) _primitives.get(_className);

      if (clazz == null) {
        throw new ClassNotFoundException(_className);
      }

      return clazz;
    } else {
      if (_type == null) {
        _type = loader.loadClass(_className);
        return _type;
      }
      else{
        return _type;
      } 
    }
  }
}
