package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.server.command.InvokeCommand;

/**
 * An event signaling the completion of a remote method invocation, on the
 * server-side.
 * 
 * @author Yanick
 */
public class ServerPostInvokeEvent implements Event {
  private long invokeDelay;
  private InvokeCommand cmd;
  private Object target, result;
  private Throwable error;

  /**
   * Creates an instance of this class with the given parameters.
   * 
   * @param target
   *          the object on which a remote method invocation was performed.
   * @param cmd
   *          the command representing the method invocation.
   * @param invokeDelay
   *          the time taken by the invocation (in millis).
   */
  public ServerPostInvokeEvent(Object target, InvokeCommand cmd, long invokeDelay) {
    this(target, cmd, invokeDelay, null);
  }

  /**
   * @param err
   *          the {@link Throwable} that was thrown at method invocation.
   * 
   * @see #ServerPostInvokeEvent(Object, InvokeCommand, long)
   */
  public ServerPostInvokeEvent(Object target, InvokeCommand cmd, long invokeDelay, Throwable err) {
    this.invokeDelay = invokeDelay;
    this.cmd = cmd;
    this.target = target;
    this.error = err;
  }

  /**
   * Returns the command representing the invocation that was made.
   * 
   * @return an {@link InvokeCommand}.
   */
  public InvokeCommand getInvokeCommand() {
    return cmd;
  }

  /**
   * Returns the object on which the method invocation was performed.
   * 
   * @return an {@link Object}.
   */
  public Object getTarget() {
    return target;
  }

  /**
   * Returns the approximate amount of time the call has taken locally.
   * 
   * @return a delay in milliseconds.
   */
  public long getInvokeDelay() {
    return invokeDelay;
  }

  /**
   * @return the {@link Throwable} that the method invocation corresponding to
   *         this instance generated, or <code>null</code> if such event
   *         occurred.
   */
  public Throwable getError() {
    return error;
  }

  /**
   * @return the result of the invocation, or <code>null</code> if no such
   *         result exists.
   */
  public Object getResultObject() {
    return result;
  }

  /**
   * @param result
   *          the result of the invocation.
   */
  public void setResultObject(Object result) {
    this.result = result;
  }
}
