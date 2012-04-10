package org.sapia.ubik.rmi.examples.interceptor;

import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;


/**
 * @author Yanick Duchesne
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
    Hub.getModules().getServerRuntime().getDispatcher().addInterceptor(ServerPreInvokeEvent.class,
      new HitCountInterceptor());
  }
}
