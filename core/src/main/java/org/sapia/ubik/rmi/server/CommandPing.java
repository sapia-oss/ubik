package org.sapia.ubik.rmi.server;

import org.sapia.ubik.rmi.server.command.RMICommand;

/**
 * An instance of this class is sent to ping a Ubik RMI server.
 * 
 * @author Yanick Duchesne
 */
public class CommandPing extends RMICommand {
  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    return new Boolean(true);
  }
}
