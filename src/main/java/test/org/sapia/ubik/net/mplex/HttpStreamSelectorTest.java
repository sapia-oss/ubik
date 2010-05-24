package org.sapia.ubik.net.mplex;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;


/**
 * Class documentation
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpStreamSelectorTest extends TestCase {
  private static final byte[] ubikHeader;
  private static final byte[] simpleHttpHeader;
  private static final byte[] complexHttpHeader;

  static {
    byte[] data1 = new byte[0];
    byte[] data2 = new byte[0];
    byte[] data3 = new byte[0];

    try {
      data1   = "UBIK!#@/$abcdefg".getBytes("UTF-8");
      data2   = "POST / HTTP/1.1\nHost: localhost\n".getBytes("UTF-8");
      data3   = "GET /server/mplex/test?param1=value1&param2=value2 HTTP/1.1\nHost: localhost\n".getBytes(
          "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } finally {
      ubikHeader          = data1;
      simpleHttpHeader    = data2;
      complexHttpHeader   = data3;
    }
  }

  /**
   * Creates a new HttpStreamSelectorTest instance.
   */
  public HttpStreamSelectorTest(String aName) {
    super(aName);
  }

  public void testDefaultSelector() throws Exception {
    HttpStreamSelector selector = new HttpStreamSelector();
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(selector.selectStream(simpleHttpHeader));
    assertTrue(selector.selectStream(complexHttpHeader));
  }

  public void testHttpMethodSelector() throws Exception {
    HttpStreamSelector selector = new HttpStreamSelector("POST", null);
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(selector.selectStream(simpleHttpHeader));
    assertTrue(!selector.selectStream(complexHttpHeader));

    selector = new HttpStreamSelector("GET", null);
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(!selector.selectStream(simpleHttpHeader));
    assertTrue(selector.selectStream(complexHttpHeader));

    selector = new HttpStreamSelector("OPTIONS", null);
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(!selector.selectStream(simpleHttpHeader));
    assertTrue(!selector.selectStream(complexHttpHeader));
  }

  public void testRequestPatternSelector() throws Exception {
    HttpStreamSelector selector = new HttpStreamSelector(null, "/");
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(selector.selectStream(simpleHttpHeader));
    assertTrue(selector.selectStream(complexHttpHeader));

    selector = new HttpStreamSelector(null, "/server/mplex");
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(!selector.selectStream(simpleHttpHeader));
    assertTrue(selector.selectStream(complexHttpHeader));

    selector = new HttpStreamSelector(null, "/ubik");
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(!selector.selectStream(simpleHttpHeader));
    assertTrue(!selector.selectStream(complexHttpHeader));
  }

  public void testHttpMethodAndRequestPatternSelector()
    throws Exception {
    HttpStreamSelector selector = new HttpStreamSelector("POST", "/");
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(selector.selectStream(simpleHttpHeader));
    assertTrue(!selector.selectStream(complexHttpHeader));

    selector = new HttpStreamSelector("GET", "/");
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(!selector.selectStream(simpleHttpHeader));
    assertTrue(selector.selectStream(complexHttpHeader));

    selector = new HttpStreamSelector("POST", "/server/mplex");
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(!selector.selectStream(simpleHttpHeader));
    assertTrue(!selector.selectStream(complexHttpHeader));

    selector = new HttpStreamSelector("GET", "/server/mplex");
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(!selector.selectStream(simpleHttpHeader));
    assertTrue(selector.selectStream(complexHttpHeader));

    selector = new HttpStreamSelector("POST", "/ubik");
    assertTrue(!selector.selectStream(ubikHeader));
    assertTrue(!selector.selectStream(simpleHttpHeader));
    assertTrue(!selector.selectStream(complexHttpHeader));
  }
}
