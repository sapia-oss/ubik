package org.sapia.ubik.net;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.concurrent.NamedThreadFactory;
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

  private Category log = Log.createCategory(getClass());
  private ConfigurableExecutor delegate;
  private AtomicInteger threadCount = new AtomicInteger();

  /**
   * @param name
   *          the name of the threads that will be created (to distinguish among
   *          the different threads, a counter is appended to the name for each
   *          thread).
   * @param daemon
   *          if <code>true</code>, the threads created by this pool will be set
   *          as daemon.
   * @param ThreadingConfiguration
   *          a {@link ThreadingConfiguration}.
   */
  protected WorkerPool(String name, boolean daemon, ThreadingConfiguration configuration) {
    this(NamedThreadFactory.createWith(name).setDaemon(true), configuration);
  }

  /**
   * @param factory
   *          the {@link ThreadFactory} to use internally.
   * @param coreSize
   *          the number of "core" threads that are created (if <= 0, a default
   *          of 5 is created).
   * @param ThreadingConfiguration
   *          a {@link ThreadingConfiguration}.
   */
  protected WorkerPool(ThreadFactory factory, ThreadingConfiguration configuration) {
    delegate = new ConfigurableExecutor(configuration, factory);
  }

  public void submit(final W work) {
    delegate.execute(new Runnable() {
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
    delegate.shutdownNow();
  }

  /**
   * @return a new {@link Work}.
   */
  protected abstract Worker<W> newWorker();

}
