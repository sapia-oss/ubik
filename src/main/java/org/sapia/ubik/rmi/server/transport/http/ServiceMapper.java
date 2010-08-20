package org.sapia.ubik.rmi.server.transport.http;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;
import simple.http.load.Service;
import simple.http.serve.Context;
import simple.http.serve.FileContext;
import simple.http.serve.FileEngine;
import simple.util.parse.URIParser;


/**
 * An instance of this class maps context paths to Simple services (as specified by the Simple API).
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServiceMapper implements ProtocolHandler {
  private FileContext _context;
  private FileEngine  _engine = new FileEngine(_context = new FileContext(
          new File(System.getProperty("user.dir"))));
  private Map _services = new HashMap();

  public ServiceMapper() {
  }

  /**
   * @return this instance's <code>Context</code> object.
   */
  public Context getContext() {
    return _context;
  }

  public void setBaseDir(File baseDir) {
    _engine = new FileEngine(_context = new FileContext(baseDir));
  }

  /**
   * Adds the given service to this object; internally maps it to the given
   * context path.
   *
   * @param contextPath the path that follows the host:port in a HTTP URL.
   * @param svc a <code>Service</code> instance, as specified by the Simple API.
   */
  public void addService(String contextPath, Service svc) {
    if (contextPath == null) {
      contextPath = "/";
    }

    if (!contextPath.startsWith("/")) {
      contextPath = "/" + contextPath;
    }

    _services.put(contextPath, svc);
  }

  /**
   * @see simple.http.ProtocolHandler#handle(simple.http.Request, simple.http.Response)
   */
  public void handle(Request req, Response res) {
    URIParser uri = new URIParser();
    uri.parse(req.getURI());

    if (uri.getPath() != null) {
      Service svc = (Service) _services.get(uri.getPath().getPath());

      if (svc != null) {
        svc.handle(req, res);
      }
    }
  }
}
