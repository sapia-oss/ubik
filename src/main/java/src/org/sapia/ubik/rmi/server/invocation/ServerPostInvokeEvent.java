package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.interceptor.Event;


/**
 * An event signaling the completion of a remote method invocation,
 * on the server-side.
 *
 * @author Yanick
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerPostInvokeEvent implements Event {
  private long          _invokeDelay;
  private InvokeCommand _cmd;
  private Object        _target, _result;
  private Throwable     _error;

  /**
   * Creates an instance of this class with the given parameters.
   *
   * @param target the object on which a remote method invocation was performed.
   * @param cmd the command representing the method invocation.
   * @param invokeDelay the time taken by the invocation (in millis).
   */
  ServerPostInvokeEvent(Object target, InvokeCommand cmd, long invokeDelay) {
    _invokeDelay   = invokeDelay;
    _cmd           = cmd;
    _target        = target;
  }
  
  /**
   * @param err the {@link Throwable} that was thrown at method invocation.
   * 
   * @see #ServerPostInvokeEvent(Object, InvokeCommand, long)
   */
  ServerPostInvokeEvent(Object target, InvokeCommand cmd, long invokeDelay, Throwable err) {
    _invokeDelay   = invokeDelay;
    _cmd           = cmd;
    _target        = target;
    _error         = err;
  }  

  /**
   * Returns the command representing the invocation that was made.
   *
   * @return an <code>InvokeCommand</code>.
   */
  public InvokeCommand getInvokeCommand() {
    return _cmd;
  }

  /**
   * Returns the object on which the method invocation was performed.
   *
   * @return an <code>Object</code>.
   */
  public Object getTarget() {
    return _target;
  }

  /**
   * Returns the approximate amount of time the call has taken
   * locally.
   *
   * @return a delay in milliseconds.
   */
  public long getInvokeDelay() {
    return _invokeDelay;
  }
  
  /**
   * @return the {@link Throwable} that the method invocation corresponding
   * to this instance generated, or <code>null</code> if such event occurred.
   */
  public Throwable getError(){
    return _error;
  }
  
  /**
   * @return the result of the invocation, or <code>null</code> if no such
   * result exists.
   */
  public Object getResultObject(){
    return _result;
  }
  
  /**
   * @param result the result of the invocation.
   */
  public void setResultObject(Object result){
    _result = result;
  }
}
