package org.sapia.ubik.net.mplex;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Implements the logic to select a stream of serialized Java objects.
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
public class ObjectStreamSelector implements StreamSelector {
  /**
   * Magic number that is written to the stream header.
   */
  final static short STREAM_MAGIC = (short) 0xaced;

  /**
   * Version number that is written to the stream header.
   */
  final static short STREAM_VERSION = 5;

  /**
   * Creates a new ObjectStreamSelector instance.
   */
  public ObjectStreamSelector() {
  }

  /**
   * Selects or not a stream by analyzing the header of the stream passed in.
   * 
   * @param header
   *          The first 64 bytes of the stream.
   * @return True if the header is accepted by this selector, false otherwise.
   */
  public boolean selectStream(byte[] header) {
    DataInputStream data = new DataInputStream(new ByteArrayInputStream(header));

    try {
      if ((data.readShort() != STREAM_MAGIC) || (data.readShort() != STREAM_VERSION)) {
        return false;
      } else {
        return true;
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();

      return false;
    }
  }
}
