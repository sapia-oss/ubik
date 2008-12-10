package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;


/**
 * Models a command that is executed asynchronously.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class AsyncCommand implements Executable {
  private String        _cmdId;
  private VmId          _caller;
  private ServerAddress _from;
  private Command       _cmd;
  private Object        _response;

  /**
   * Constructor for AsyncCommandWrapper.
   *
   * @param the unique identifier of this command.
   * @param from the <code>ServerAddress</code> corresponding to
   * the server to call back, and from which this command originates.
   * @param cmd the <code>Command</code> instance to wrap.
   */
  public AsyncCommand(String cmdId, VmId caller, ServerAddress from, Command cmd) {
    _cmdId    = cmdId;
    _from     = from;
    _cmd      = cmd;
    _caller   = caller;
  }

  /**
   * Returns the identifier of this command.
   *
   * @return the identifier of this command.
   */
  public String getCmdId() {
    return _cmdId;
  }

  /**
   * Returns the address of the server to call back, and from which
   * this command originates.
   *
   * @return a <code>ServerAddress<code>.
   */
  public ServerAddress getFrom() {
    return _from;
  }

  /**
   * Returns the caller's VM identifier.
   *
   * @return a <code>VmId</code>
   */
  public VmId getCallerVmId() {
    return _caller;
  }

  /**
   * Returns this instance's wrapped command.
   *
   * @return a <code>Command</code>.
   */
  public Command getCommand() {
    return _cmd;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.Executable#execute()
   */
  public Object execute() throws Throwable {
    return _cmd.execute();
  }
}
