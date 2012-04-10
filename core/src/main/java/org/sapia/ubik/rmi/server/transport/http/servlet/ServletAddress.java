package org.sapia.ubik.rmi.server.transport.http.servlet;

import org.sapia.ubik.net.Uri;
import org.sapia.ubik.net.UriSyntaxException;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;


/**
 * Models the network address of a servlet.
 *
 * @author Yanick Duchesne
 */
public class ServletAddress extends HttpAddress {
  /**
   * Do not call; used for externalization only.
   */
  public ServletAddress() {
  }

  /**
   * Creates an instance of this class with the given parameter.
   *
   * @param servletUrl the URL of the servlet to connect to.
   * @throws UriSyntaxException if the URL could not be internally parsed.
   */
  public ServletAddress(String servletUrl) throws UriSyntaxException {
    super(ServletConsts.DEFAULT_SERVLET_TRANSPORT_TYPE, Uri.parse(servletUrl));
  }

  /**
   * Intended to be overridden by eventual subclasses.
   */
  protected ServletAddress(String transportType, String servletUrl)
    throws UriSyntaxException {
    super(transportType, Uri.parse(servletUrl));
  }
}
