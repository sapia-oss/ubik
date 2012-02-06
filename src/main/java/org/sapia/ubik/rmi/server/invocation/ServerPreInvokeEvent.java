package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.server.command.InvokeCommand;


/**
 * An event signaling that a remote method call has been received on the server-side.
 *
 * @author Yanick
 */
public class ServerPreInvokeEvent implements Event {
  private InvokeCommand cmd;
  private Object        target;
  private long          start = System.currentTimeMillis();

  /**
   * @param cmd the command representing the method invocation that will be performed.
   * @param target the object on which the invocation will be performed.
   */
  public ServerPreInvokeEvent(InvokeCommand cmd, Object target) {
    this.cmd      = cmd;
    this.target   = target;
  }

  /**
   * Returns the command that represents the method invocation that will
   * be performed.
   *
   * @return an {@link InvokeCommand}.
   */
  public InvokeCommand getInvokeCommand() {
    return cmd;
  }

  /**
   * Sets the target of the invocation (client-applications can thus
   * substitute the original target with the one passed in).
   *
   * @param target a new target on which to perform the invocation.
   */
  public void setTarget(Object target) {
    this.target = target;
  }

  /**
   * Returns the object on which to perform the method call.
   *
   * @return an {@link Object}.
   */
  public Object getTarget() {
    return target;
  }

  /**
   * Returns the approximate time at which the invocation
   * was received.
   *
   * @return a time in milliseconds.
   */
  public long getInvokeTime() {
    return start;
  }
}
