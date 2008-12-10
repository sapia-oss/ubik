package org.sapia.ubik.rmi.examples;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JdkFoo extends UnicastRemoteObject implements Foo {
  public JdkFoo() throws java.rmi.RemoteException {
  }

  /**
   * @see org.sapia.ubik.rmi.Foo#getBar()
   */
  public Bar getBar() throws RemoteException {
    return new JdkBar();
  }

  public static void main(String[] args) {
    try {
      java.rmi.registry.Registry reg = LocateRegistry.createRegistry(1098);
      Foo                        f = new JdkFoo();
      java.rmi.Naming.bind("rmi://localhost:1098/Foo", f);

      System.out.println("JdkFoo started...");

      while (true) {
        Thread.sleep(10000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
