package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;

/**
 * Wraps a {@link Command} for asynchronous execution.
 * 
 * @author Yanick Duchesne
 */
public class AsyncCommand implements Executable {
  private long cmdId;
  private VmId caller;
  private ServerAddress from;
  private Command cmd;

  /**
   * Constructor for AsyncCommandWrapper.
   * 
   * @param cmdId
   *          the unique identifier of this command.
   * @param caller
   *          the {@link VmId} corresponding to the JVM from which the command
   *          originates.
   * @param from
   *          the {@link ServerAddress} corresponding to the server to call
   *          back, and from which this command originates.
   * @param cmd
   *          the {@link Command} instance to wrap.
   */
  public AsyncCommand(long cmdId, VmId caller, ServerAddress from, Command cmd) {
    this.cmdId = cmdId;
    this.from = from;
    this.cmd = cmd;
    this.caller = caller;
  }

  /**
   * @return the identifier of this command.
   */
  public long getCmdId() {
    return cmdId;
  }

  /**
   * Returns the address of the server to call back, and from which this command
   * originates.
   * 
   * @return a {@link ServerAddress}.
   */
  public ServerAddress getFrom() {
    return from;
  }

  /**
   * Returns the caller's VM identifier.
   * 
   * @return a {@link VmId}
   */
  public VmId getCallerVmId() {
    return caller;
  }

  /**
   * Returns this instance's wrapped command.
   * 
   * @return a <code>Command</code>.
   */
  public Command getCommand() {
    return cmd;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.Executable#execute()
   */
  public Object execute() throws Throwable {
    return cmd.execute();
  }
}
