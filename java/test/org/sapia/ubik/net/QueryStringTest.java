package org.sapia.ubik.net;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class QueryStringTest extends TestCase {
  /**
   * Constructor for QueryStringTest.
   */
  public QueryStringTest(String name) {
    super(name);
  }

  public void testParse() throws Exception {
    String      s  = "/some/path?name1=value1&name2=value2";
    QueryString qs = QueryString.parse(s);
    super.assertEquals("/some/path", qs.getPath());
    super.assertEquals("value1", qs.getParameter("name1"));
    super.assertEquals("value2", qs.getParameter("name2"));
  }

  public void testToString() throws Exception {
    String      s  = "/some/path?name1=value1&name2=value2";
    QueryString qs = QueryString.parse(s);
    qs = QueryString.parse(qs.toString());
    super.assertEquals("/some/path", qs.getPath());
    super.assertEquals("value1", qs.getParameter("name1"));
    super.assertEquals("value2", qs.getParameter("name2"));
  }

  public void testInstantiate() throws Exception {
    QueryString qs = new QueryString("/some/path");
    qs.addParameter("name1", "value1");
    qs.addParameter("name2", "value2");
    super.assertEquals("/some/path", qs.getPath());
    super.assertEquals("value1", qs.getParameter("name1"));
    super.assertEquals("value2", qs.getParameter("name2"));
  }
}
