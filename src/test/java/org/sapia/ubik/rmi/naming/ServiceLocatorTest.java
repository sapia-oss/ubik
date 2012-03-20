package org.sapia.ubik.rmi.naming;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 */
public class ServiceLocatorTest extends TestCase {
  public ServiceLocatorTest(String name) {
    super(name);
  }

  public void testRegister() throws Exception {
    ServiceLocator.registerHandler("test", new TestHandler());
    ServiceLocator.lookup("test://localhost:80/test");
  }
}
