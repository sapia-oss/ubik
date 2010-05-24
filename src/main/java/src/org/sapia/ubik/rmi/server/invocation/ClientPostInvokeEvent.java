package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.interceptor.Event;


/**
 * This event is generated on the client-side, after a given
 * method has been invoked.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClientPostInvokeEvent implements Event {
  private InvokeCommand _toInvoke;
  private Object        _return;

  /**
   * Creates an instance of this class.
   *
   * @param invocable the <code>Invocable</code> command to was sent to
   * the server to perform the method call.
   * @param toReturn the object returned by the remote method
   * (might be <code>null</code> (if the called method returned
   * such); must be <code>Throwable</code> if the called method
   * threw and exception).
   */
  public ClientPostInvokeEvent(InvokeCommand toInvoke, Object toReturn) {
    _toInvoke   = toInvoke;
    _return     = toReturn;
  }

  /**
   * Returns the command to invoke.
   *
   * @return an <code>InvokeCommand</code> instance.
   */
  public InvokeCommand getCommand() {
    return _toInvoke;
  }

  /**
   * Sets this instance return object. The passed in
   * object must be an instance of the original one.
   * <p>
   * This method call is ignored if the original object
   * reference is <code>null</code>, meaning that the
   * method actually returned nothing, or has a return
   * type of <code>void</code>.
   *
   * @param an <code>Object</code>.
   */
  public void setReturnObject(Object object) {
    if (_return != null) {
      _return = object;
    }
  }

  /**
   * Returns the object that was returned by the remote
   * method call.
   * <p>
   * Important: the object that will be returned might be an
   * instance of <code>Throwable</code>, provided the method
   * threw an exception.
   *
   * @return an <code>Object</code>, or <code>null</code> if
   * the invoked remote method returned nothing, or has a
   * return type of <code>void</code>.
   */
  public Object getReturnObject() {
    return _return;
  }
}
