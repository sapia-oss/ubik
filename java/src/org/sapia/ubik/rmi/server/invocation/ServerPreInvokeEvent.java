package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.interceptor.Event;


/**
 * An event signaling that a remote method call has been
 * received on the server-side.
 *
 * @author Yanick
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerPreInvokeEvent implements Event {
  private InvokeCommand _cmd;
  private Object        _target;
  private long          _start = System.currentTimeMillis();

  /**
   * @param cmd the command representing the method invocation that will be performed.
   * @param target the object on which the invocation will be performed.
   */
  ServerPreInvokeEvent(InvokeCommand cmd, Object target) {
    _cmd      = cmd;
    _target   = target;
  }

  /**
   * Returns the command that represents the method invocation that will
   * be performed.
   *
   * @return an <code>InvokeCommand</code>.
   */
  public InvokeCommand getInvokeCommand() {
    return _cmd;
  }

  /**
   * Sets the target of the invocation (client-applications can thus
   * substitute the original target with the one passed in).
   *
   * @param target a new target on which to perform the invocation.
   */
  public void setTarget(Object target) {
    _target = target;
  }

  /**
   * Returns the object on which to perform the method call.
   *
   * @return an <code>Object</code>.
   */
  public Object getTarget() {
    return _target;
  }

  /**
   * Returns the approximate time at which the invocation
   * was received.
   *
   * @return a time in milliseconds.
   */
  public long getInvokeTime() {
    return _start;
  }
}
