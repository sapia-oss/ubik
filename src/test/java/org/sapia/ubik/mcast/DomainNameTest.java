package org.sapia.ubik.mcast;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DomainNameTest extends TestCase {
  /**
   * Constructor for DomainNameTest.
   * @param arg0
   */
  public DomainNameTest(String arg0) {
    super(arg0);
  }

  public void testContains() throws Exception {
    DomainName dn1 = DomainName.parse("n1");
    DomainName dn2 = DomainName.parse("n1/n2");
    DomainName dn3 = DomainName.parse("n1/n2/n3");
    DomainName dn4 = DomainName.parse("n1/n4");

    super.assertTrue(dn2.contains(dn1));
    super.assertTrue(dn3.contains(dn1));
    super.assertTrue(dn3.contains(dn2));
    super.assertTrue(dn1.contains(dn1));

    super.assertTrue(!dn1.contains(dn2));
    super.assertTrue(!dn2.contains(dn3));
    super.assertTrue(!dn2.contains(dn4));
    super.assertTrue(!dn3.contains(dn4));
  }

  public void testParse() throws Exception {
    DomainName dn1 = DomainName.parse("n1");
    DomainName dn2 = DomainName.parse("n1/n2");
    DomainName dn3 = DomainName.parse("n1/n2/n3");

    super.assertEquals(1, dn1.size());
    super.assertEquals(2, dn2.size());
    super.assertEquals(3, dn3.size());
  }
}
