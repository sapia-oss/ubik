package org.sapia.ubik.rmi.naming.remote.proxy;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.sapia.ubik.rmi.naming.ServiceHandler;
import org.sapia.ubik.rmi.naming.ServiceLocator;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;


/**
 * A {@link ServiceHandler} that looks up a service using given parameters.
 * 
 * @author Yanick Duchesne
 */
@SuppressWarnings(value="unchecked")
public class JNDIHandler implements ServiceHandler {
  /**
   * @see org.sapia.ubik.rmi.naming.ServiceHandler#handleLookup(String, int, String, Map)
   */
  public Object handleLookup(String host, int port, String path, Map attributes)
    throws NameNotFoundException, NamingException {
    RemoteInitialContextFactory fac = new RemoteInitialContextFactory();
    Hashtable                   env = new Hashtable(attributes);
    env.put(Context.PROVIDER_URL,
      ServiceLocator.UBIK_SCHEME + "://" + host + ":" + port + "/");

    Context context = null;

    try {
      context = fac.getInitialContext(env);

      return context.lookup(path);
    } finally {
      if (context != null) {
        context.close();
      }
    }
  }
}
