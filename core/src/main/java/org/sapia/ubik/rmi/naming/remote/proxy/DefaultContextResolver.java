/*
 * DefaultContextResolver.java
 *
 * Created on August 17, 2005, 3:29 PM
 *
 */

package org.sapia.ubik.rmi.naming.remote.proxy;

import javax.naming.Context;

import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.RemoteContextProvider;
import org.sapia.ubik.rmi.server.Hub;

/**
 * A default {@link Context} resolver.
 * 
 * @author yduchesne
 */
public class DefaultContextResolver implements ContextResolver {

  /** Creates a new instance of DefaultContextResolver */
  public DefaultContextResolver() {
  }

  public RemoteContext resolve(org.sapia.ubik.net.ServerAddress addr) throws java.rmi.RemoteException {
    Object remote = Hub.connect(addr);
    if (remote instanceof RemoteContextProvider) {
      return ((RemoteContextProvider) remote).getRemoteContext();
    } else {
      return (RemoteContext) remote;
    }
  }

  public RemoteContext resolve(String host, int port) throws java.rmi.RemoteException {
    Object remote = Hub.connect(host, port);
    if (remote instanceof RemoteContextProvider) {
      return ((RemoteContextProvider) remote).getRemoteContext();
    } else {
      return (RemoteContext) remote;
    }
  }

}
