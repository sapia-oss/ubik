package org.sapia.ubik.util;

import java.io.PrintStream;

/**
 * This interface defines basic debugging behavior.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2005 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Debug {

  /**
   * Outputs the given message, originating from an instance of
   * the given class.
   * 
   * @param caller the <code>Class</code> from which the call is made.
   * @param msg the message to output.
   */
  public void out(Class caller, String msg);
  
  /**
   * Outputs the given message and error, originating from an instance of
   * the given class.
   * 
   * @param caller the <code>Class</code> from which the call is made.
   * @param msg the message to output.
   * @param err a <code>Throwable</code>.
   */
  public void out(Class caller, String msg, Throwable err);
  
  /**
   * @return the <code>PrintStream</code> this is used to perform the output.
   */
  public PrintStream out();
  
  /**
   * @return <code>true</code> if debugging is on.
   */
  public boolean on();
  
  /**
   * @param on <code>true</code> if debugging should be turned on,
   * false otherwise.
   */
  public void on(boolean on);

}
