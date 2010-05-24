package org.sapia.ubik.rmi.naming;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServiceLocatorTest extends TestCase {
  public ServiceLocatorTest(String name) {
    super(name);
  }

  public void testRegister() throws Exception {
    System.setProperty("ubik.rmi.naming.service.handler.test",
      "org.sapia.ubik.rmi.naming.TestHandler");

    ServiceLocator.lookup("test://localhost:80/test");
  }
}
