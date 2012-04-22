package org.sapia.ubik.rmi.server.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.sapia.ubik.rmi.server.ShutdownException;
import org.sapia.ubik.rmi.server.stats.Hits;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.util.Delay;


/**
 * Models a queue of {@link Executable} instances.
 *
 * @author Yanick Duchesne
 */
public class ExecQueue<T extends Executable> {
  
  private LinkedList<T>    queue    = new LinkedList<T>();
  private volatile boolean shutdown;
  private Hits             insertionsBerSecond = Stats.getInstance().getHitsBuilder(
                                                   getClass(), 
                                                   "InsertionsPerSec", 
                                                   "Number of queue insertions per second")
                                                   .perSecond().build();
  
  private Hits             removalsBerSecond   = Stats.getInstance().getHitsBuilder(
                                                   getClass(), 
                                                   "RemovalsPerSec", 
                                                   "Number of queue removals per second")
                                                   .perSecond().build();  
  
  public ExecQueue() {
    super();
  }

  /**
   * Adds an {@link Executable} to this queue.
   *
   * @param toExecute an {@link Executable}.
   */
  public synchronized void add(T toExecute) {
    if (shutdown) {
      throw new ShutdownException();
    }
    insertionsBerSecond.hit();
    queue.add(toExecute);
    notify();
  }

  /**
   * Removes all the {@link Executable} from this queue and
   * returns them; if the queue is empty, this method blocks until
   * a new item is added.
   *
   * @return a {@link List} of {@link Executable}s.
   */
  public synchronized List<T> removeAll()
    throws InterruptedException, ShutdownException {
    while (queue.size() == 0) {
      if (shutdown) {
        notify();
        throw new ShutdownException();
      }
      wait();
    }

    List<T> toReturn = new ArrayList<T>(queue);
    removalsBerSecond.hit(toReturn.size());
    queue.clear();

    return toReturn;
  }
  
  /**
   * Returns this instance's queued item (upon returning, this instance's queue
   * is cleared from all its items).
   * @return the {@link List} of {@link Executable}s that this instance 
   * holds.
   */
  public synchronized List<T> getAll() {
    List<T> toReturn = new ArrayList<T>(queue);
    removalsBerSecond.hit(toReturn.size());
    queue.clear();

    return toReturn;
  }

  /**
   * Shuts down this instance.
   *
   * @param timeout a timeout in millis. If this queue still has pending
   * objects after the timeout is reached, this method returns.
   */
  public synchronized void shutdown(long timeout) throws InterruptedException {
    shutdown = true;
    notify();
    Delay timer = new Delay(timeout);
    while (queue.size() > 0 && !timer.isOver()) {
      wait(timer.remainingNotZero());
    }
  }

  /**
   * Returns this queue's size.
   *
   * @return this queue's size (the number of items in this queue).
   */
  public int size() {
    return queue.size();
  }

  /**
   * Removes the first {@link Executable} from this queue and returns it.
   *
   * @return an {@link Executable}.
   */
  public synchronized T remove()
    throws InterruptedException, ShutdownException {
    while (queue.size() == 0) {
      if (shutdown) {
        notify();
        throw new ShutdownException();
      }

      wait();
    }

    removalsBerSecond.hit();
    return queue.remove(0);
  }
  
  public boolean isShutdown() {
	  return shutdown;
  }
  
}
