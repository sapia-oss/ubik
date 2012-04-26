package org.sapia.ubik.rmi.server.transport.http;

import org.sapia.ubik.net.Worker;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.WorkerPool;


/**
 * A pool of {@link HttpRmiServerThread} instances.
 *
 * @author Yanick Duchesne
 */
class HttpRmiServerThreadPool extends WorkerPool<Request> {
  /**
   * Creates an instance of this class.
   */
  HttpRmiServerThreadPool(boolean daemon, int maxSize) {
    super("http.HttpServerThread", daemon, maxSize);
  }
  
  @Override
  protected Worker<Request> newWorker() {
    return new HttpRmiServerThread();
  }
}
