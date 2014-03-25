package org.sapia.ubik.rmi.naming;

import java.net.UnknownHostException;

import org.sapia.ubik.rmi.naming.remote.JNDIConsts;
import org.sapia.ubik.util.Localhost;

public class UrlMaker {

  private UrlMaker() {
  }
  
  /**
   * @return a JNDI lookup base URL.
   */
  public static String makeLookupBaseUrl() {
    try {
      return makeLookupBaseUrl(Localhost.getPreferredLocalAddress().getHostAddress(), JNDIConsts.DEFAULT_PORT);
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Could not acquire local host address", e);
    }
  }
  
  /**
   * @return a JNDI lookup base URL.
   */
  public static String makeLookupBaseUrl(int port) {
    try {
      return makeLookupBaseUrl(Localhost.getPreferredLocalAddress().getHostAddress(), port);
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Could not acquire local host address", e);
    }
  }
  
  /**
   * @return a JNDI lookup base URL.
   */
  public static String makeLookupBaseUrl(String host, int port) {
    return "ubik://" + host + ":" + port + "/";
  }
}
