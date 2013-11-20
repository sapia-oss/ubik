package org.sapia.ubik.net.mplex;

import java.io.UnsupportedEncodingException;

/**
 * This basic selector implementation allows you to define a selection logic
 * based on a simple string value. It can be configures with two types of
 * comparison:
 * <ol>
 * <li>The type <code>TYPE_STARTS_WITH</code> will select a stream of data only
 * if it starts with the string value of this selector.</li>
 * <li>The type <code>TYPE_CONTAINS</code> will select a stream of data only if
 * it contains the entire string value of this selector.</li>
 * </ol>
 * 
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 *         <dl>
 *         <dt><b>Copyright:</b>
 *         <dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *         Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 *         <dt><b>License:</b>
 *         <dd>Read the license.txt file of the jar or visit the <a
 *         href="http://www.sapia-oss.org/license.html"
 *         target="sapia-license">license page</a> at the Sapia OSS web site</dd>
 *         </dt>
 *         </dl>
 */
public class SimpleStreamSelector implements StreamSelector {
  public static final byte TYPE_STARTS_WITH = 1;
  public static final byte TYPE_CONTAINS = 2;
  private String _theValue;
  private int _theType;

  /**
   * Creates a new SimpleStreamSelector instance.
   */
  public SimpleStreamSelector(String aValue, int aType) {
    _theValue = aValue;
    _theType = aType;
  }

  /**
   * Selects or not a stream by analyzing the header of the stream passed in.
   * 
   * @param header
   *          The first 64 bytes of the stream.
   * @return True if the header is accepted by this selector, false otherwise.
   */
  public boolean selectStream(byte[] header) {
    try {
      String aStringValue = new String(header, 0, header.length, "UTF-8");

      if (_theType == TYPE_CONTAINS) {
        return aStringValue.indexOf(_theValue) >= 0;
      } else if (_theType == TYPE_STARTS_WITH) {
        return aStringValue.startsWith(_theValue);
      } else {
        return false;
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();

      return false;
    }
  }
}
