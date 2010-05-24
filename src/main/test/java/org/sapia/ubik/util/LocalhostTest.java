/*
 * LocalhostTest.java
 * JUnit based test
 *
 * Created on August 18, 2005, 9:13 AM
 */

package org.sapia.ubik.util;

import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.sapia.ubik.rmi.Consts;

/**
 *
 * @author yduchesne
 */
public class LocalhostTest extends TestCase {
  
  public LocalhostTest(String testName) {
    super(testName);
  }

  /**
   * Test of getLocalAddress method, of class org.sapia.ubik.util.Localhost.
   */
  public void testGetLocalAddress() throws Exception{
    String addr = Localhost.getLocalAddress().getHostAddress();
    super.assertTrue(!addr.equals("localhost"));
    super.assertTrue(!addr.equals("127.0.0.1"));
    super.assertTrue(!addr.equals("0.0.0.0"));
  }
  
  public void testIsLocalAddress(){
    Pattern p = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d+\\.\\d+");
    super.assertTrue(!Localhost.isLocalAddress(p, "127.0.0.1"));
    super.assertTrue(!Localhost.isLocalAddress(p, "10.10.10.1"));
    super.assertTrue(!Localhost.isLocalAddress(p, "localhost"));
    super.assertTrue(Localhost.isLocalAddress(p, "192.168.0.10"));
  }

  
}
