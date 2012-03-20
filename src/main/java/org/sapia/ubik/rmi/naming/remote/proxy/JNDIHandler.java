package org.sapia.ubik.rmi.naming.remote.proxy;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.naming.ServiceHandler;
import org.sapia.ubik.rmi.naming.ServiceLocator;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;


/**
 * A {@link ServiceHandler} that looks up a service using given parameters.
 * 
 * @author Yanick Duchesne
 */
public class JNDIHandler implements ServiceHandler {
	
	private Category log = Log.createCategory(getClass());
	
  /**
   * @see org.sapia.ubik.rmi.naming.ServiceHandler#handleLookup(String, int, String, Map)
   */
  public Object handleLookup(String host, int port, String path, Map<String, String> attributes)
    throws NameNotFoundException, NamingException {
  	
  	log.debug("Looking up %s from %s:%s", path, host, port);
    RemoteInitialContextFactory fac = new RemoteInitialContextFactory();
    Hashtable<String, String>  env = new Hashtable<String, String>(attributes);
    env.put(Context.PROVIDER_URL,
      ServiceLocator.UBIK_SCHEME + "://" + host + ":" + port + "/");

    Context context = null;

    try {
    	log.debug("Connecting to JNDI");
      context = fac.getInitialContext(env);
      log.debug("Performing lookup");
      Object toReturn = context.lookup(path);
    	log.debug("Lookup completed");
    	return toReturn;
    } finally {
      if (context != null) {
        context.close();
      }
    }
  }
}
