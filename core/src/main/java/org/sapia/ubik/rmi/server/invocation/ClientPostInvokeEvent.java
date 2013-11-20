package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.server.command.InvokeCommand;

/**
 * This event is generated on the client-side, after a given method has been
 * invoked.
 * 
 * @author Yanick Duchesne
 */
public class ClientPostInvokeEvent implements Event {
  private InvokeCommand toInvoke;
  private Object returnValue;

  /**
   * Creates an instance of this class.
   * 
   * @param toInvoke
   *          the {@link InvokeCommand} that was sent to the server to perform
   *          the method call.
   * @param toReturn
   *          the object returned by the remote method (might be
   *          <code>null</code> (if the called method returned such); must be
   *          {@link Throwable} if the called method threw and exception).
   */
  public ClientPostInvokeEvent(InvokeCommand toInvoke, Object toReturn) {
    this.toInvoke = toInvoke;
    this.returnValue = toReturn;
  }

  /**
   * Returns the command to invoke.
   * 
   * @return an {@link InvokeCommand} instance.
   */
  public InvokeCommand getCommand() {
    return toInvoke;
  }

  /**
   * Sets this instance return object. The passed in object must be an instance
   * of the original one.
   * <p>
   * This method call is ignored if the original object reference is
   * <code>null</code>, meaning that the method actually returned nothing, or
   * has a return type of <code>void</code>.
   * 
   * @param object
   *          the {@link Object} that consists of the return value of the method
   *          call.
   */
  public void setReturnObject(Object object) {
    if (returnValue != null) {
      returnValue = object;
    }
  }

  /**
   * Returns the object that was returned by the remote method call.
   * <p>
   * Important: the object that will be returned might be an instance of
   * {@link Throwable}, provided the method threw an exception.
   * 
   * @return an {@link Object}, or <code>null</code> if the invoked remote
   *         method returned nothing, or has a return type of <code>void</code>.
   */
  public Object getReturnObject() {
    return returnValue;
  }
}
