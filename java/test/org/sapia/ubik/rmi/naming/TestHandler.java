package org.sapia.ubik.rmi.naming;

import java.util.Map;

import javax.naming.*;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestHandler implements ServiceHandler {
  /**
   * @see org.sapia.ubik.rmi.naming.ServiceHandler#handleLookup(String, int, String, Map)
   */
  public Object handleLookup(String host, int port, String path, Map attributes)
    throws NameNotFoundException, NamingException {
    return "test";
  }
}
