package org.sapia.ubik.rmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.rmi.server.RMIClassLoader;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Utils {
  
  public static Object deserialize(byte[] bytes)
  throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
      bytes));
    Object            o = ois.readObject();
    
    return o;
  }
  
  public static Object deserialize(byte[] bytes, ClassLoader loader)
  throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStreamEx(new ByteArrayInputStream(
      bytes), loader);
    Object            o = ois.readObject();
    
    return o;
  }
  
  public static Object deserialize(byte[] bytes, ClassLoader loader, String codebase)
  throws IOException, ClassNotFoundException {
    RMIClassLoader.getClassLoader(codebase);
    ObjectInputStream ois = new ObjectInputStreamEx(new ByteArrayInputStream(
      bytes), loader);
    Object            o = ois.readObject();
    
    return o;
  }  
  
  public static byte[] serialize(Object o) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream    ous = new ObjectOutputStream(bos);
    ous.writeObject(o);
    ous.flush();
    ous.close();
    
    return bos.toByteArray();
  }
  
  static final class ObjectInputStreamEx extends ObjectInputStream {
    private ClassLoader _loader;
    
    ObjectInputStreamEx(InputStream is, ClassLoader loader)
    throws IOException {
      super(is);
      _loader = loader;
    }
    
    /**
     * @see java.io.ObjectInputStream#resolveClass(ObjectStreamClass)
     */
    protected Class resolveClass(ObjectStreamClass desc)
    throws IOException, ClassNotFoundException {
      if (_loader != null) {
        return _loader.loadClass(desc.getName());
      } else {
        return super.resolveClass(desc);
      }
    }
  }
}
