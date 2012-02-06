package org.sapia.ubik.rmi.server.command;


/**
 * This interface is based on the command pattern; it models an executable
 * unit of work.
 *
 * @author Yanick Duchesne
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
