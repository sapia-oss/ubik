package org.sapia.ubik.rmi.server;

import junit.framework.TestCase;

import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.test.TestUtils;

import java.lang.reflect.Proxy;


/**
 * @author Yanick Duchesne
 * 15-Sep-2003
 */
public class RemoteRefReliableTest extends TestCase {
  /**
   * Constructor for RemoteRefExTest.
   * @param arg0
   */
  public RemoteRefReliableTest(String arg0) {
    super(arg0);
  }

  public void testSerialize() throws Exception {
    RemoteRefReliable ref = new RemoteRefReliable(new OID(0),
        new TCPAddress("localhost", 0));
    ref.setUp("ubik://somehost/someName");

    Object proxy = Proxy.newProxyInstance(Thread.currentThread()
                                                .getContextClassLoader(),
        TestUtils.getInterfacesFor(java.rmi.Remote.class), ref);
    TestUtils.serialize(proxy);
  }

  public void testDeserialize() throws Exception {
    RemoteRefReliable ref = new RemoteRefReliable(new OID(0),
        new TCPAddress("localhost", 0));
    ref.setUp("ubik://somehost/someName");

    Object proxy = Proxy.newProxyInstance(Thread.currentThread()
                                                .getContextClassLoader(),
        TestUtils.getInterfacesFor(java.rmi.Remote.class), ref);
    byte[] b = TestUtils.serialize(proxy);
    proxy = TestUtils.deserialize(b);
  }
}
