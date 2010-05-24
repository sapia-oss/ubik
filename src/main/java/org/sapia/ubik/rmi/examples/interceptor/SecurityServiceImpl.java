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
public class SecurityServiceImpl implements SecurityService, Interceptor {
  /**
   * @see org.sapia.ubik.rmi.examples.interceptor.SecurityService#call()
   */
  public void call() {
    System.out.println("call performed...");
  }

  public void onServerPreInvokeEvent(ServerPreInvokeEvent evt) {
    throw new RuntimeException("call forbidden");
  }

  public static void main(String[] args) {
    try {
      SecurityServiceImpl svc = new SecurityServiceImpl();
      Hub.serverRuntime.addInterceptor(ServerPreInvokeEvent.class, svc);
      Hub.exportObject(svc, 6767);

      while (true) {
        Thread.sleep(10000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
