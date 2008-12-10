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
public class SimpleStreamSelectorTest extends TestCase {
  private static final byte[] header;

  static {
    byte[] data = new byte[0];

    try {
      data = "UBIK!#@/$abcdefg".getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } finally {
      header = data;
    }
  }

  /**
   * Creates a new SimpleStreamSelectorTest instance.
   */
  public SimpleStreamSelectorTest(String aName) {
    super(aName);
  }

  public void testNonSelectedStream() throws Exception {
    assertTrue(!new SimpleStreamSelector("HTTP",
        SimpleStreamSelector.TYPE_CONTAINS).selectStream(header));
    assertTrue(!new SimpleStreamSelector("HTTP",
        SimpleStreamSelector.TYPE_STARTS_WITH).selectStream(header));
  }

  public void testSelectedInitialStream() throws Exception {
    assertTrue(new SimpleStreamSelector("UBIK",
        SimpleStreamSelector.TYPE_CONTAINS).selectStream(header));
    assertTrue(new SimpleStreamSelector("UBIK",
        SimpleStreamSelector.TYPE_STARTS_WITH).selectStream(header));
  }

  public void testSelectedContentStream() throws Exception {
    assertTrue(new SimpleStreamSelector("abcdef",
        SimpleStreamSelector.TYPE_CONTAINS).selectStream(header));
    assertTrue(!new SimpleStreamSelector("abcdef",
        SimpleStreamSelector.TYPE_STARTS_WITH).selectStream(header));
  }
}
