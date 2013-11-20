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
  public static final String HTTP_TRANSPORT_TYPE = "http";

  /**
   * Corresponds to the <code>ubik.rmi.transport.http.port</code> property, used
   * to specify the port on which a HTTP Ubik RMI server should be bound (for a
   * given <code>HttpTransportProvider</code> instance.
   */
  public static final String HTTP_PORT_KEY = "ubik.rmi.transport.http.port";

  /**
   * Corresponds to the <code>ubik.rmi.transport.http.bind.address</code>
   * property, used to specify the address on which a HTTP Ubik RMI server
   * should be bound (for a given <code>HttpTransportProvider</code> instance.
   */
  public static final String HTTP_BIND_ADDRESS_KEY = "ubik.rmi.transport.http.bind.address";

  /**
   * Corresponds to the
   * <code>ubik.rmi.transport.http.client.max-connections</code> property, used
   * to specify the max number of connections that the HTTP client will pool.
   */
  public static final String HTTP_CLIENT_MAX_CONNECTIONS_KEY = "ubik.rmi.transport.http.client.max-connections";

  /**
   * This constant specifies the default context path.
   */
  public static final String CONTEXT_PATH = "/ubik";

  /**
   * This constant specifies the default number of connections that the HTTP
   * client will pool (set to 25).
   * 
   * @see #HTTP_CLIENT_MAX_CONNECTIONS_KEY
   */
  public static final int DEFAULT_MAX_CLIENT_CONNECTIONS = 25;
}
