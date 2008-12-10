package org.sapia.ubik.net;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MaxThreadReachedException extends Exception {
  /**
   * Constructor for MaxThreadReachedException.
   * @param arg0
   */
  public MaxThreadReachedException(String arg0) {
    super(arg0);
  }

  /**
   * Constructor for MaxThreadReachedException.
   * @param arg0
   * @param arg1
   */
  public MaxThreadReachedException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  /**
   * Constructor for MaxThreadReachedException.
   * @param arg0
   */
  public MaxThreadReachedException(Throwable arg0) {
    super(arg0);
  }

  /**
   * Constructor for MaxThreadReachedException.
   */
  public MaxThreadReachedException() {
    super();
  }
}
