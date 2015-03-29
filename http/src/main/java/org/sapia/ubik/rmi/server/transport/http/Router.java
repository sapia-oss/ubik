package org.sapia.ubik.rmi.server.transport.http;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Log;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * An instance of this class maps context paths to {@link Handler}s. If no
 * {@link Handler} could be found for a given request path at runtime, it will
 * use a catch-all {@link Handler} (configured with
 * {@link #setCatchAllHandler(Handler)}). The latter's responsibility is to
 * handle the unmatched requests in a way it sees fit (this allows for
 * implementing a delegation mechanism).
 * <p>
 * The default catch-all handler simply returns an empty response with status
 * code 404 if a request could not be matched.
 * 
 * @author Yanick Duchesne
 */
public class Router implements Handler {

  private static final int STATUS_NOT_FOUND = 404;

  private Map<String, Handler> handlers = new ConcurrentHashMap<String, Handler>();
  private Handler catchAllHandler = new DefaultCatchAllHandler();

  public Router() {
  }

  /**
   * Adds the given service to this object; internally maps it to the given
   * context path.
   * 
   * @param contextPath
   *          the path that follows the host:port in a HTTP URL.
   * @param handler
   *          a {@link Handler} instance.
   */
  public void addHandler(String contextPath, Handler handler) {
    if (contextPath == null) {
      contextPath = "/";
    }
    if (!contextPath.startsWith("/")) {
      contextPath = "/" + contextPath;
    }
    handlers.put(contextPath, handler);
  }

  /**
   * @param catchAllHandler
   *          the {@link Handler} if the request could not be matched to a
   *          {@link Handler} that was added through the
   *          {@link #addHandler(String, Handler)} method.
   */
  public void setCatchAllHandler(Handler catchAllHandler) {
    this.catchAllHandler = catchAllHandler;
  }

  @Override
  public void handle(Request req, Response res) {
    Handler handler = (Handler) handlers.get(req.getPath().getPath());
    if (handler != null) {
      handler.handle(req, res);
    } else {
      catchAllHandler.handle(req, res);
    }
  }

  @Override
  public void shutdown() {
    for (Handler handler : handlers.values()) {
      handler.shutdown();
    }
  }

  // --------------------------------------------------------------------------

  class DefaultCatchAllHandler implements Handler {

    @Override
    public void handle(Request req, Response res) {
      res.setCode(STATUS_NOT_FOUND);
      try {
        res.commit();
        res.close();
      } catch (IOException e) {
        Log.warning(DefaultCatchAllHandler.class, "Error committing response", e);
      }
    }

    @Override
    public void shutdown() {
    }
  }
}
