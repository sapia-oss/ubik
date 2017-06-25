package org.sapia.ubik.rmi.server.command;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.javasimon.Counter;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Pause;

/**
 * This class implements a client-side response queue that internally keeps
 * response locks.
 * 
 * @author Yanick Duchesne
 */
public class CallbackResponseQueue {

  private Category log = Log.createCategory(getClass());
  private Map<Long, ResponseLock> responseLocks = new ConcurrentHashMap<Long, ResponseLock>();
  private volatile boolean shutdown;
  private Counter callbackCount = Stats.createCounter(getClass(), "CallbackCount", "Number of callbacks performed");

  /**
   * @param timeout
   *          the timeout allowed for the shut down to occur.
   */
  synchronized void shutdown(long timeout) {
    if (shutdown) {
      return;
    }
    shutdown = true;

    Pause timer = new Pause(timeout);

    while (responseLocks.size() > 0 && !timer.isOver()) {
      try {
        wait(timer.remainingNotZero());
      } catch (InterruptedException e) {

      }
    }
  }

  /***
   * Creates a response lock and returns it (internally maps the response lock
   * to a unique identifier which is also assigned to the lock.
   * 
   * @return a {@link ResponseLock}.
   */
  public synchronized ResponseLock createResponseLock() {
    ResponseLock lock = new ResponseLock(this);

    Assertions.illegalState(responseLocks.containsKey(lock.getId()), "Response lock already exists for: %s", lock.getId());

    callbackCount.increase();
    log.debug("Creating response lock %s", lock.getId());
    responseLocks.put(lock.getId(), lock);
    return lock;
  }

  /**
   * This method is called to notify this queue about incoming responses.
   * 
   * @param resp a {@link Response} received asynchronously.
   */
  public void onResponse(Response resp) {
    ResponseLock lock;
    lock = responseLocks.get(resp.getId());
    if (lock != null) {
      log.debug("Received response for response lock %s", lock.getId());
      lock.setResponse(resp.get());
    } else {
      log.debug("Received response for lock %s, but lock is null (probably timed out)", resp.getId());
    }
  }

  /**
   * @return the number of {@link ResponseLock}s currently held by this
   *         instance.
   */
  int size() {
    return responseLocks.size();
  }

  synchronized void removeLock(long id) {
    responseLocks.remove(id);

    // synchronizing with shutdown() method
    if (shutdown) {
      notify();
    }
  }

}
