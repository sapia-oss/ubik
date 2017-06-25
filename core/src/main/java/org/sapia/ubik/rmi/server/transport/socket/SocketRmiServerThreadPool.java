package org.sapia.ubik.rmi.server.transport.socket;

import java.util.concurrent.ExecutorService;

import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.Worker;
import org.sapia.ubik.net.WorkerPool;
import org.sapia.ubik.rmi.server.Hub;

/**
 * Implements a pool of {@link SocketRmiServerThread}s in a
 * {@link SocketRmiServer} instance.
 * 
 * @author yduchesne
 */
public class SocketRmiServerThreadPool extends WorkerPool<Request> {

  public SocketRmiServerThreadPool( ExecutorService executor) {
    super(executor);
  }

  @Override
  protected Worker<Request> newWorker() {
    return new SocketRmiServerThread(Hub.getModules().getServerRuntime().getDispatcher());
  }
}
