package org.sapia.ubik.rmi.server.transport.http.servlet;


/**
 * Holds constants.
 *
 * @author Yanick Duchesne
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
