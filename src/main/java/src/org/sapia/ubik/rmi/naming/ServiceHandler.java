package org.sapia.ubik.rmi.naming;

import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;


/**
 * An instance of this class performs lookups on behalf of
 * the <code>ServiceLocator</code>.
 *
 * @see ServiceLocator
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ServiceHandler {
  /**
   * Handles the lookup for the given path, given the host and port.
   *
   * @param host the host to look up.
   * @param port the port on which to connect. If the port corresponds to
   * the <code>UNDEFINED_PORT</code> constant, it means that the caller has
   * not specified any port (a necessary feature in order to support well-known
   * ports).
   * @param path the path of the object to look up.
   * @param attributes a <code>Map</code> of attributes - the latter correspond to the
   * query string parameters that is parsed out of the URL that is specified to the
   * <code>ServiceLocator</code>.
   *
   * @return an <code>Object</code>
   * @throws NameNotFoundException if no object could be found for the
   * given path.
   * @throws NamingException if an error occurs while performing the lookup.
   */
  public Object handleLookup(String host, int port, String path, Map attributes)
    throws NameNotFoundException, NamingException;
}
