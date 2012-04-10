package org.sapia.ubik.rmi.examples.replication;

import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.replication.ReplicatedInvoker;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClientSideInterceptor implements Interceptor {
  private ReplicatedInvoker _invoker;

  public ClientSideInterceptor(ReplicatedInvoker invoker) {
    _invoker = invoker;
  }

  public void onClientPreInvokeEvent(ClientPreInvokeEvent evt) {
    System.out.println("Command will be replicated...");
    evt.setCommand(new ReplicatedCommandEx(evt.getCommand(), null, _invoker));
  }
}
