package org.sapia.ubik.rmi.server.transport.socket;

import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ThreadPool;

/**
 * Implements a pool of {@link SocketRmiServerThread}s in a {@link SocketRmiServer} instance.
 *
 * @author Yanick Duchesne
 */
public class SocketRmiServerThreadPool extends ThreadPool<Request> {
  
  /**
   * @param name
   * @param daemon
   * @param maxSize
   */
  public SocketRmiServerThreadPool(String name, boolean daemon, int maxSize) {
    super(name, daemon, maxSize);
  }

  /**
   * @see org.sapia.ubik.net.ThreadPool#newThread()
   */
  protected PooledThread<Request> newThread(String name) throws Exception {
    return new SocketRmiServerThread(name);
  }
}
