package org.sapia.ubik.rmi.examples.interceptor;

import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HitCountInterceptor implements Interceptor {
  private int _count;

  public synchronized void onServerPreInvokeEvent(ServerPreInvokeEvent evt) {
    _count++;
  }

  public synchronized int getCount() {
    return _count;
  }

  public static void main(String[] args) {
    Hub.serverRuntime.dispatcher.addInterceptor(ServerPreInvokeEvent.class,
      new HitCountInterceptor());
  }
}
