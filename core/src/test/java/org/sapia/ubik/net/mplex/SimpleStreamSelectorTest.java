package org.sapia.ubik.net.mplex;

import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
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
    assertTrue(!new SimpleStreamSelector("HTTP", SimpleStreamSelector.TYPE_CONTAINS).selectStream(header));
    assertTrue(!new SimpleStreamSelector("HTTP", SimpleStreamSelector.TYPE_STARTS_WITH).selectStream(header));
  }

  public void testSelectedInitialStream() throws Exception {
    assertTrue(new SimpleStreamSelector("UBIK", SimpleStreamSelector.TYPE_CONTAINS).selectStream(header));
    assertTrue(new SimpleStreamSelector("UBIK", SimpleStreamSelector.TYPE_STARTS_WITH).selectStream(header));
  }

  public void testSelectedContentStream() throws Exception {
    assertTrue(new SimpleStreamSelector("abcdef", SimpleStreamSelector.TYPE_CONTAINS).selectStream(header));
    assertTrue(!new SimpleStreamSelector("abcdef", SimpleStreamSelector.TYPE_STARTS_WITH).selectStream(header));
  }
}
