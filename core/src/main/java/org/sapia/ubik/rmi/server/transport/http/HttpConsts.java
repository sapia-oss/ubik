package org.sapia.ubik.rmi.server.transport.http;


/**
 * Holds constants.
 *
 * @author Yanick Duchesne
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
   * Corresponds to the <code>ubik.rmi.transport.http.client.max-connections</code> property, used
   * to specify the max number of connections that the HTTP client will pool.
   */
  public static final String HTTP_CLIENT_MAX_CONNECTIONS_KEY = "ubik.rmi.transport.http.client.max-connections";  

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
  
  /**
   * This constant specifies the default number of connections that the HTTP client will pool (set to 25).
   * 
   * @see #HTTP_CLIENT_MAX_CONNECTIONS_KEY
   */
  public static final int DEFAULT_MAX_CLIENT_CONNECTIONS = 25;  
}
