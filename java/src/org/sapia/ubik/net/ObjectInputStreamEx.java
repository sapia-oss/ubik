package org.sapia.ubik.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import java.util.HashMap;


/**
 * An <code>ObjectInputStream</code> that allows to specify an
 * application-defined class loader.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ObjectInputStreamEx extends ObjectInputStream {
  /** table mapping primitive type names to corresponding class objects */
  private static final HashMap _primitives = new HashMap(8, 1.0F);

  static {
    _primitives.put("boolean", boolean.class);
    _primitives.put("byte", byte.class);
    _primitives.put("char", char.class);
    _primitives.put("short", short.class);
    _primitives.put("int", int.class);
    _primitives.put("long", long.class);
    _primitives.put("float", float.class);
    _primitives.put("double", double.class);
    _primitives.put("void", void.class);
  }

  ClassLoader _loader;

  /**
   * Creates an instance of this class with the given parameters.
   *
   * @param is the stream to this instance will encapsulate.
   * @param loader a <code>ClassLoader</code>
   */
  public ObjectInputStreamEx(InputStream is, ClassLoader loader)
    throws java.io.IOException {
    super(is);
    _loader = loader;
  }

  /**
   * @see java.io.ObjectInputStream#resolveClass(ObjectStreamClass)
   */
  protected Class resolveClass(ObjectStreamClass clazz)
    throws IOException, ClassNotFoundException {
    Class resolved;
    resolved = (Class) _primitives.get(clazz.getName());

    if (resolved == null) {
      return _loader.loadClass(clazz.getName());
    } else {
      return resolved;
    }
  }
}
