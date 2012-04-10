package org.sapia.ubik.rmi.examples;

import java.util.HashMap;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class StatelessUbikBar implements Bar {
  HashMap map      = new HashMap();
  int     hashcode;

  public StatelessUbikBar(int hashCode) throws java.rmi.RemoteException {
    map.put("foo", "bar!!!");
    hashcode = hashCode;
  }

  /**
   * @see org.sapia.ubik.rmi.Bar#getMsg()
   */
  public String getMsg() {
    System.out.println("getMsg()... @" + Integer.toHexString(hashcode));

    return ((String) map.get("foo")) + "@" + Integer.toHexString(hashcode);
  }
}
