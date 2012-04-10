package org.sapia.ubik.rmi.examples.interceptor;

import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;


/**
 * @author Yanick Duchesne
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
      Hub.getModules().getServerRuntime().getDispatcher().addInterceptor(ServerPreInvokeEvent.class, svc);
      Hub.exportObject(svc, 6767);

      while (true) {
        Thread.sleep(10000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
