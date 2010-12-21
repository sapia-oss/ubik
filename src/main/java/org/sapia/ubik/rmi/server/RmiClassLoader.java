/*
 * RmiClassLoader.java
 *
 * Created on June 30, 2005, 7:29 PM
 */

package org.sapia.ubik.rmi.server;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.security.SecureClassLoader;

/**
 *
 * @author yduchesne
 */
public class RmiClassLoader extends SecureClassLoader{
  
  private String _codebase;
  
  /** Creates a new instance of RmiClassLoader */
  public RmiClassLoader(ClassLoader parent, String codebase) {
    super(parent);
  }
  
  /**
   * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
   */
  public synchronized Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
    try{
      return RMIClassLoader.loadClass(_codebase, name, getParent());
    }catch(MalformedURLException e){
      throw new ClassNotFoundException(name);
    }
  }
}
