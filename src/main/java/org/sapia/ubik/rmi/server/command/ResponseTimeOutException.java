package org.sapia.ubik.rmi.server.command;


/**
 * Thrown when an expected asynchronous response is expected but
 * does not come in before a specified timeout.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ResponseTimeOutException extends RuntimeException {
  
  static final long serialVersionUID = 1L;
  
  /**
   * Constructor for ResponseTimeOutException.
   */
  public ResponseTimeOutException() {
    super();
  }
}
