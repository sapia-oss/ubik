package org.sapia.ubik.rmi.server.command;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.stats.Hits;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.util.Delay;


/**
 * This class implements a client-side response queue that internally keeps response locks.
 *
 * @author Yanick Duchesne
 */
public class CallbackResponseQueue {
  
  private Category                  log             = Log.createCategory(getClass());
  private Map<Long, ResponseLock>   responseLocks   = new ConcurrentHashMap<Long, ResponseLock>();
  private volatile boolean          shutdown;
  private Hits                      callbacksPerSec = Stats.getInstance()
                                                        .getHitsBuilder(getClass(), "CallbacksPerSec", "Callbacks per second")
                                                        .perSecond().build();

  /**
   * @param timeout the timeout allowed for the shut down to occur.
   */
  synchronized void shutdown(long timeout) {
    if(shutdown) {
      return;
    }
    shutdown = true;

    Delay timer = new Delay(timeout);

    while (responseLocks.size() > 0 && !timer.isOver()) {
      try {
        wait(timer.remainingNotZero());
      } catch (InterruptedException e) {
        
      }
    }    
  }
  
  /***
   * Creates a response lock and returns it (internally maps the response
   * lock to a unique identifier which is also assigned to the lock.
   *
   * @return a {@link ResponseLock}.
   */
  public synchronized ResponseLock createResponseLock() {
    ResponseLock lock = new ResponseLock(this);

    if (responseLocks.containsKey(lock.getId())) {
      throw new IllegalStateException("Response lock already exists for: " + lock.getId());
    }
     
    callbacksPerSec.hit();
    log.debug("Creating response lock %s", lock.getId());
    responseLocks.put(lock.getId(), lock);
    return lock;
  }

  /**
   * This method is called to notify this queue about incoming
   * responses.
   *
   * @param responses a {@link List} of {@link Response} instances.
   */
  public void onResponses(List<Response> expectedResponses) {
    ResponseLock lock;
    for (Response resp : expectedResponses) {
      lock = responseLocks.get(resp.getId());
      if (lock != null) {
        log.debug("Received response for response lock %s", lock.getId());        
        lock.setResponse(resp.get());
      } else {
        log.debug("Received response for lock %s, but lock is null (probably timed out)", resp.getId());
      }
    }
  }

  /**
   * @return the number of {@link ResponseLock}s currently held by this instance.
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
