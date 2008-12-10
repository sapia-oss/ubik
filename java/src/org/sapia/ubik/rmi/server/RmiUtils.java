/*
 * RmiUtils.java
 *
 * Created on June 30, 2005, 7:54 PM
 */

package org.sapia.ubik.rmi.server;

import java.lang.reflect.Proxy;

import org.sapia.ubik.rmi.Consts;

/**
 *
 * @author yduchesne
 */
public class RmiUtils {
  
  public static final String CODE_BASE = System.getProperty("java.rmi.server.codebase");
  
  public static final boolean MARSHALLING = (System.getProperty(Consts.MARSHALLING) != null) &&
    System.getProperty(Consts.MARSHALLING).equals("true");  
  
  public  static final boolean CODE_DOWNLOAD = (System.getProperty(Consts.ALLOW_CODE_DOWNLOAD) != null) &&
    System.getProperty(Consts.ALLOW_CODE_DOWNLOAD).equals("true");    
  
  /**
   * @return <code>true</code> if the given object is a stub (an instance of
   * the <code>Stub</code> interface).
   *
   * @see Stub
   */
  public static boolean isStub(Object o){
    return o instanceof Stub;
  }

  /**
   * @return the <code>StubInvocationHandler</code> corresponding to the given
   * stub, or <code>null</code> if no such stub exists.
   */
  public static StubInvocationHandler getInvocationHandlerFor(Object stub){
    if(stub instanceof Stub && Proxy.isProxyClass(stub.getClass())){
      return (StubInvocationHandler)Proxy.getInvocationHandler(stub);
    }
    return null;
  }
  
}
