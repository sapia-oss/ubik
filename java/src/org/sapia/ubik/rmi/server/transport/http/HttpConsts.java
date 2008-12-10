package org.sapia.ubik.rmi.server.transport.http;


/**
 * Holds constants.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface HttpConsts {
  /**
   * This constant specifies the default HTTP port: 8080.
   */
  public static final int DEFAULT_HTTP_PORT = 8080;

  /**
   * This constant specifies the default HTTP "transport type" identifier: http.
   */
  public static final String DEFAULT_HTTP_TRANSPORT_TYPE = "http";

  /**
   * Corresponds to the <code>ubik.rmi.transport.http.port</code> property, used
   * to specify the port on which a HTTP Ubik RMI server should be bound (for
   * a given <code>HttpTransportProvider</code> instance.
   */
  public static final String HTTP_PORT_KEY = "ubik.rmi.transport.http.port";

  /**
   * The key of the property that corresponds to the server URL that is made available to clients.
   * (<code>ubik.rmi.transport.http.url</code>).
   */
  public static final String SERVER_URL_KEY = "ubik.rmi.transport.http.url";

  /**
   * The key of the property that corresponds to the path under which the Ubik request handler is bound.
   * (<code>ubik.rmi.transport.http.path</code>).
   */
  public static final String PATH_KEY = "ubik.rmi.transport.http.path";

  /**
   * This constant specifies the default context path.
   */
  public static final String DEFAULT_CONTEXT_PATH = "/ubik";
}
