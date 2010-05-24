package org.sapia.ubik.rmi.examples.http;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;

import org.mortbay.jetty.servlet.ServletHandler;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.examples.Foo;
import org.sapia.ubik.rmi.examples.UbikFoo;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.server.transport.http.servlet.ServletConsts;
import org.sapia.ubik.rmi.server.transport.http.servlet.ServletTransportProvider;

import java.io.IOException;

import java.rmi.RemoteException;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpFooServlet extends HttpServlet {
  // this would not be hard-coded like this normally - but 
  // rather configured in servlet init parameters.
  static final String              SERVLET_URL = "http://localhost:8080/ubik";
  private Foo                      _foo;
  private ServletTransportProvider _provider;

  public void init(ServletConfig conf) throws ServletException {
    _provider = new ServletTransportProvider();
    TransportManager.registerProvider(_provider);

    Properties props = new Properties();

    // we would normally get value for this property from init parameters...
    props.setProperty(ServletConsts.SERVLET_URL_KEY, SERVLET_URL);

    // this tells ubik "under" which transport our object will be exported 
    props.setProperty(Consts.TRANSPORT_TYPE,
      ServletConsts.DEFAULT_SERVLET_TRANSPORT_TYPE);

    try {
      _foo = new UbikFoo();
      Hub.exportObject(_foo, props);
    } catch (RemoteException e) {
      throw new ServletException("Could not export Foo", e);
    }
  }

  protected void service(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
    _provider.handleRequest(req, res);
  }

  /**
   * @see javax.servlet.GenericServlet#destroy()
   */
  public void destroy() {
    try {
      Hub.shutdown(30000);
    } catch (Exception e) {
      getServletContext().log("Exception caught performing Hub shutdown", e);
    }
  }

  public static void main(String[] args) {
    try {
      HttpServer server = new HttpServer();
      server.addListener(":8080");

      HttpContext    context = server.getContext("/");
      ServletHandler handler = new ServletHandler();
      handler.addServlet("Ubik", "/ubik/*",
        "org.sapia.ubik.rmi.examples.http.HttpFooServlet");
      context.addHandler(handler);

      server.start();

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
