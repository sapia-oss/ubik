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
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
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
