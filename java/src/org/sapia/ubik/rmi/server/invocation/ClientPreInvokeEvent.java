package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.interceptor.Event;


/**
 * This instance is generated on the client-size prior to a
 * remote method call.
 * <p>
 * Intercepting this event allows to implement substitution behavior,
 * such as:
 * <pre>
 * ...
 *  InvokeCommand inv = event.getInvocable();
 *  Credentials cre = SecurityController.getCredentials();
 *  SecureInvocationCommand sec = new SecureInvokationCommand(inv);
 *  evt.setCommand(sec);
 * ...
 * </pre>
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClientPreInvokeEvent implements Event {
  private InvokeCommand _cmd;

  public ClientPreInvokeEvent(InvokeCommand cmd) {
    _cmd = cmd;
  }

  /**
   * Return the command that will be sent to the server.
   *
   * @return am <code>InvokeCommand</code> instance.
   */
  public InvokeCommand getCommand() {
    return _cmd;
  }

  /**
   * Sets the command that will be sent to the server.
   *
   * @param an <code>InvokeCommand</code>.
   */
  public void setCommand(InvokeCommand cmd) {
    _cmd = cmd;
  }
}
