package org.sapia.ubik.rmi.naming.remote;

/**
 * Holds constants.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Consts extends org.sapia.ubik.rmi.Consts{
  
  public static final int    DEFAULT_PORT   = 1099;
  
  public static final String JNDI_SERVER_PUBLISH = "ubik/rmi/naming/server/publish";
  public static final String JNDI_SERVER_DISCO   = "ubik/rmi/naming/server/disco";
  public static final String JNDI_CLIENT_PUBLISH = "ubik/rmi/naming/client/publish";
}
