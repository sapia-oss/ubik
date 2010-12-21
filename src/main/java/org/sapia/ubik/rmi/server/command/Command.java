package org.sapia.ubik.rmi.server.command;


/**
 * A command class - implements the Command pattern.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class Command implements Executable, java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
  /**
   * Constructor for Command.
   */
  public Command() {
    super();
  }

  /***
   * This method executes this command.
   *
   * @return any value returned by this method.
   * @throws Throwable if an error occurs executing this command.
   */
  public abstract Object execute() throws Throwable;
}
