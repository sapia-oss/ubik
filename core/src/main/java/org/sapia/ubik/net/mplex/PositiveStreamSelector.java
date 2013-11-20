package org.sapia.ubik.net.mplex;

/**
 * This is an implementation of a selector that always return true.
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
public class PositiveStreamSelector implements StreamSelector {
  /**
   * Creates a new PositiveStreamSelector instance.
   */
  public PositiveStreamSelector() {
  }

  /**
   * Selects or not a stream by analyzing the header of the stream passed in.
   * 
   * @param header
   *          The first 64 bytes of the stream.
   * @return True if the header is accepted by this selector, false otherwise.
   */
  public boolean selectStream(byte[] header) {
    return true;
  }
}
