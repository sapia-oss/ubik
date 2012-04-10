package org.sapia.ubik.rmi.server.transport.http;

import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;


/**
 * Adds mandatory HTTP headers to every response - before it is returned.
 *
 * @author Yanick Duchesne
 */
class HeaderHandler implements ProtocolHandler {
  private ProtocolHandler handler;

  HeaderHandler(ProtocolHandler toWrap) {
    handler = toWrap;
  }

  /**
   * @see simple.http.ProtocolHandler#handle(simple.http.Request, simple.http.Response)
   */
  public void handle(Request req, Response res) {
    res.set("Server", "DemoServer/1.0 (Simple)");
    res.setDate("Date", System.currentTimeMillis());
    res.setDate("Last-Modified", System.currentTimeMillis());
    handler.handle(req, res);
  }
}
