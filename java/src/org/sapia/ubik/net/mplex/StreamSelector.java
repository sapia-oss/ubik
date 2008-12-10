package org.sapia.ubik.net.mplex;


/**
 * This interface derfines the contract of a selector that defines, from a preview
 * of a stream of data, if it can handle the entire stream or not. This interface is
 * used by the <code>MultiplexServerSocket</code> to determine which socket connector
 * will handle a new socket connection.
 *
 * @see MultiplexServerSocket
 * @see HttpStreamSelector
 * @see ObjectStreamSelector
 * @see SimpleStreamSelector
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface StreamSelector {
  /**
   * Selects or not a stream by analyzing the header of the stream passed in.
   * 
   * @param header The first bytes of the stream.
   * @return True if the header is accepted by this selector, false otherwise.
   */
  public boolean selectStream(byte[] header);
}
