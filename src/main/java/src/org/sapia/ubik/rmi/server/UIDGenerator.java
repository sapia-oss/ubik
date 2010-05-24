package org.sapia.ubik.rmi.server;


/**
 * A generator of unique longs for this VM.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class UIDGenerator {
  private static long _uid    = System.currentTimeMillis();
  private static short _offset;

  /**
   * @return a unique long for this VM.
   */
  public synchronized static long createdUID() {
    long uid = _uid + (_offset++);

    if (uid < 0 || _offset < 0) {
      _uid      = System.currentTimeMillis();
      _offset   = 0;
      uid       = _uid + (_offset++);
    } 
    return uid;
  }
}
