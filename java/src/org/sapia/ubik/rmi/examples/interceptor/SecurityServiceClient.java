package org.sapia.ubik.rmi.examples.interceptor;

import org.sapia.ubik.rmi.server.Hub;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SecurityServiceClient {
  public static void main(String[] args) {
    try {
      SecurityService sec = (SecurityService) Hub.connect("localhost", 6767);
      sec.call();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
