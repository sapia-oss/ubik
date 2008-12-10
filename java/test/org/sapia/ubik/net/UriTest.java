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
public class UriTest extends TestCase {
  /**
   * Constructor for UriTest.
   */
  public UriTest(String name) {
    super(name);
  }

  public void testParseHttpUri() throws Exception {
    String s = "http://www.sapia-oss.org";
    Uri    u = Uri.parse(s);
    super.assertEquals("http", u.getScheme());
    super.assertEquals(Uri.UNDEFINED_PORT, u.getPort());
    super.assertEquals("www.sapia-oss.org", u.getHost());
    s   = "http://www.sapia-oss.org/";
    u   = Uri.parse(s);
    super.assertEquals("http", u.getScheme());
    super.assertEquals(Uri.UNDEFINED_PORT, u.getPort());
    super.assertEquals("www.sapia-oss.org", u.getHost());
  }

  public void testParseHttpUriWithPort() throws Exception {
    String s = "http://www.sapia-oss.org:8080";
    Uri    u = Uri.parse(s);
    super.assertEquals("http", u.getScheme());
    super.assertEquals("www.sapia-oss.org", u.getHost());
    super.assertEquals(8080, u.getPort());
    s   = "http://www.sapia-oss.org:8080/";
    u   = Uri.parse(s);
    super.assertEquals("http", u.getScheme());
    super.assertEquals("www.sapia-oss.org", u.getHost());
    super.assertEquals(8080, u.getPort());
  }

  public void testParseHttpUriWithPath() throws Exception {
    String s = "http://www.sapia-oss.org/index.html";
    Uri    u = Uri.parse(s);
    super.assertEquals("http", u.getScheme());
    super.assertEquals(Uri.UNDEFINED_PORT, u.getPort());
    super.assertEquals("www.sapia-oss.org", u.getHost());
    super.assertEquals("/index.html", u.getQueryString().getPath());
    s   = "http://www.sapia-oss.org:8080/index.html";
    u   = Uri.parse(s);
    super.assertEquals("http", u.getScheme());
    super.assertEquals(8080, u.getPort());
    super.assertEquals("www.sapia-oss.org", u.getHost());
    super.assertEquals("/index.html", u.getQueryString().getPath());
  }

  public void testParseHttpUriWithQueryString() throws Exception {
    String s = "http://www.sapia-oss.org/index.html?name1=value1&name2=value2";
    Uri    u = Uri.parse(s);
    super.assertEquals("http", u.getScheme());
    super.assertEquals(Uri.UNDEFINED_PORT, u.getPort());
    super.assertEquals("www.sapia-oss.org", u.getHost());
    super.assertEquals("/index.html", u.getQueryString().getPath());
    super.assertEquals(u.getQueryString().getParameter("name1"), "value1");
    super.assertEquals(u.getQueryString().getParameter("name2"), "value2");
  }

  public void testParseFileUri() throws Exception {
    String s = "file:/d:/some/file.html";
    Uri    u = Uri.parse(s);
    super.assertEquals("file", u.getScheme());
    super.assertEquals(Uri.UNDEFINED_HOST, u.getHost());
    super.assertEquals("/d:/some/file.html", u.getQueryString().getPath());
    super.assertTrue("host should not be specified",
      (u.getHost() == null) || (u.getHost().length() == 0));
    super.assertEquals(Uri.UNDEFINED_PORT, u.getPort());
  }
}
