package org.sapia.ubik.net;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;

/**
 * Encapsulates a pool of {@link Worker}s, which are called in separate threads.
 * The work submitted to the {@link #submit(Object)} method of an instance of
 * this class is delegated to a {@link Worker}'s {@link Worker#execute(Object)}
 * method.
 * 
 * @see Worker
 * @see Worker#execute(Object)
 * @author Yanick Duchesne
 */
public abstract class WorkerPool<W> {

  private Category         log         = Log.createCategory(getClass());
  private ExecutorService  executor;
  private AtomicInteger    threadCount = new AtomicInteger();

  /**
   * @param executor the {@link ExecutorService} to use internally.
   */
  protected WorkerPool(ExecutorService executor) {
    this.executor = executor;
  }

  public void submit(final W work) {
    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          threadCount.incrementAndGet();
          newWorker().execute(work);
        } catch (RuntimeException e) {
          log.info("Runtime error caught running thread", e, log.noArgs());
        } finally {
          threadCount.decrementAndGet();
        }
      }
    });
  }

  /**
   * @return the number of threads that are currently performing work (not
   *         idling).
   */
  public int getThreadCount() {
    return threadCount.get();
  }

  /**
   * Shuts down this instance.
   */
  public void shutdown() {
    executor.shutdown();
  }

  /**
   * @return a new {@link Work}.
   */
  protected abstract Worker<W> newWorker();

}
