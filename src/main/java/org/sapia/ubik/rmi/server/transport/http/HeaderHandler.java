package org.sapia.ubik.rmi.server.transport.http;

import org.simpleframework.http.core.Container;


/**
 * Adds mandatory HTTP headers to every response - before it is returned.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class HeaderHandler implements Container {
  private Container _handler;

  HeaderHandler(Container toWrap) {
    _handler = toWrap;
  }

  public void handle(org.simpleframework.http.Request req, org.simpleframework.http.Response res) {
    res.set("Server", "DemoServer/1.0 (Simple)");
    res.setDate("Date", System.currentTimeMillis());
    res.setDate("Last-Modified", System.currentTimeMillis());
    _handler.handle(req, res);
  }
}
