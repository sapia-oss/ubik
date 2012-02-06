package org.sapia.ubik.rmi.server;

import org.sapia.ubik.rmi.server.command.Command;
import org.sapia.ubik.rmi.server.command.RMICommand;


/**
 * The {@link Command} implementations from which all Ubik RMI commands inherit.
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
