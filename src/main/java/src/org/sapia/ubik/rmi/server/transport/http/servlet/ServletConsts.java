package org.sapia.ubik.rmi.server.transport.http.servlet;


/**
 * Holds constants.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ServletConsts {
  /**
   * Constant that corresponds to the default identifier of the
   * servlet transport (<code>http/servlet</code>).
   */
  public static final String DEFAULT_SERVLET_TRANSPORT_TYPE = "http/servlet";

  /**
   * Constant that correspond to the key specifying the value of the property
   * indicating the URL of the servlet that is used to receive remote method
   * calls (<code>ubik.rmi.transport.servlet.url</code>).
   */
  public static final String SERVLET_URL_KEY = "ubik.rmi.transport.servlet.url";
}
