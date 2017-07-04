package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.server.command.InvokeCommand;

/**
 * This instance is generated on the client-size prior to a remote method call.
 * <p>
 * Intercepting this event allows to implement substitution behavior, such as:
 * 
 * <pre>
 * ...
 *  InvokeCommand inv = event.getInvocable();
 *  Credentials cre = SecurityController.getCredentials();
 *  SecureInvocationCommand sec = new SecureInvokationCommand(inv);
 *  evt.setCommand(sec);
 * ...
 * </pre>
 * 
 * @author yduchesne
 */
public class ClientPreInvokeEvent {

  private InvokeCommand cmd;

  public ClientPreInvokeEvent(InvokeCommand cmd) {
    this.cmd = cmd;
  }

  /**
   * Returns the command that will be sent to the server.
   * 
   * @return an {@link InvokeCommand} instance.
   */
  public InvokeCommand getCommand() {
    return cmd;
  }

  /**
   * Sets the command that will be sent to the server.
   * 
   * @param cmd
   *          an {@link InvokeCommand}.
   */
  public void setCommand(InvokeCommand cmd) {
    this.cmd = cmd;
  }
}
