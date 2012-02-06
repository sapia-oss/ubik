package org.sapia.ubik.rmi.naming;

import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;


/**
 * An instance of this class performs lookups on behalf of
 * the {@link ServiceLocator}.
 *
 * @see ServiceLocator
 * 
 * @author Yanick Duchesne
 */
public interface ServiceHandler {
  /**
   * Handles the lookup for the given path, given the host and port.
   *
   * @param host the host to look up.
   * @param port the port on which to connect. If the port corresponds to
   * the {@link ServiceLocator#UNDEFINED_PORT} constant, it means that the caller has
   * not specified any port (a necessary feature in order to support well-known
   * ports).
   * @param path the path of the object to look up.
   * @param attributes a <code>Map</code> of attributes - the latter correspond to the
   * query string parameters that is parsed out of the URL that is specified to the
   * {@link ServiceLocator}.
   *
   * @return an <code>Object</code>
   * @throws NameNotFoundException if no object could be found for the
   * given path.
   * @throws NamingException if an error occurs while performing the lookup.
   */
  public Object handleLookup(String host, int port, String path, Map<String,String> attributes)
    throws NameNotFoundException, NamingException;
}
