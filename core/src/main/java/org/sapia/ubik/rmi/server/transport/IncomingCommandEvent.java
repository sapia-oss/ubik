package org.sapia.ubik.rmi.server.transport;

import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.server.command.RMICommand;

/**
 * Dispatched by the {@link CommandHandler} when a {@link RMICommand} is received. 
 * @author yduchesne
 *
 */
public class IncomingCommandEvent implements Event {
  
  private RMICommand command;
  
  public IncomingCommandEvent(RMICommand cmd) {
    this.command = cmd;
  }
  
  public RMICommand getCommand() {
    return command;
  }

}
