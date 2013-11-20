package org.sapia.ubik.rmi.naming;

import java.util.Map;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

/**
 * @author Yanick Duchesne
 */
public class TestHandler implements ServiceHandler {
  /**
   * @see org.sapia.ubik.rmi.naming.ServiceHandler#handleLookup(String, int,
   *      String, Map)
   */

  @Override
  public Object handleLookup(String host, int port, String path, Map<String, String> attributes) throws NameNotFoundException, NamingException {
    return "test";
  }
}
