package org.sapia.ubik.rmi.server.command;


/**
 * This interface is based on the command pattern; it models an executable
 * unit of work.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Executable {
  /**
   * Performs this instance's logic.
   *
   * @return <code>null</code> or some return value, if it applies.
   * @throws Throwable if an error occurs in this command's execution.
   */
  public Object execute() throws Throwable;
}
