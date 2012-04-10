package org.sapia.ubik.rmi.server.transport.http;

import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ThreadPool;


/**
 * A pool of {@link HttpRmiServerThread} instances.
 *
 * @author Yanick Duchesne
 */
class HttpRmiServerThreadPool extends ThreadPool<Request> {
  /**
   * Creates an instance of this class.
   */
  HttpRmiServerThreadPool(boolean daemon, int maxSize) {
    super("http.HttpServerThread", daemon, maxSize);
  }

  protected PooledThread<Request> newThread(String name) throws Exception {
    return new HttpRmiServerThread(name);
  }
}
