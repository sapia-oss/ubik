package org.sapia.ubik.net;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TCPAddressTest extends TestCase {
  public TCPAddressTest(String name) {
    super(name);
  }

  public void testEquals() {
    TCPAddress addr1 = new TCPAddress("localhost", 2222);
    TCPAddress addr2 = new TCPAddress("localhost", 2223);

    super.assertTrue(addr1.equals(addr1));
    super.assertTrue(!addr1.equals(addr2));
  }

  public void testSet() {
    TCPAddress addr1 = new TCPAddress("localhost", 2222);
    TCPAddress addr2 = new TCPAddress("localhost", 2223);
    Set        set1  = new HashSet();
    Set        set2  = new HashSet();

    set1.add(addr1);
    set2.add(addr2);

    set1.removeAll(set2);
    super.assertTrue(set1.contains(addr1));
    set1.retainAll(set2);
    super.assertTrue(!set1.contains(addr1));
    set1.add(addr1);
    set1.removeAll(set1);
    super.assertTrue(!set1.contains(addr1));
  }
}
