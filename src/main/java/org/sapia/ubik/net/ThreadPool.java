package org.sapia.ubik.net;

import java.util.ArrayList;
import java.util.List;

import org.sapia.ubik.util.Delay;
import org.sapia.ubik.util.pool.Pool;


/**
 * A pool of {@link PooledThread} instances. Inheriting classes
 * must implement the {@link #newThread()} method, which must return
 * an application-specific {@link PooledThread} instance.
 * <p>
 * Applications must use the pool in the following manner:
 *
 * <pre>
 *
 * PooledThread thread = threadPool.acquire();
 *
 * thread.exec(someData);
 *
 * </pre>
 *
 * Upon the {@link PooledThread#exec(Object)} method being called, the thread
 * immediately:
 * <ul>
 * <li>calls its own doExec</code> method;
 * <li>releases itself to its "owning" pool after the doExec() method returns.
 * </ul>
 *
 * <p>
 * Thus, applications need not be concerned about returning the passed in thread
 * to the pool.
 *
 *
 *
 * @see PooledThread
 * @see PooledThread#doExec(Object)
 * @author Yanick Duchesne
 */
public abstract class ThreadPool<T> extends Pool<PooledThread<T>> {
  private String                 name;
  private boolean                daemon;
  private volatile boolean       shuttingDown;
  private List<PooledThread<T>>  busy = new ArrayList<PooledThread<T>>();

  /**
   * Creates a thread pool.
   *
   * @param name the name of the threads that will be created (to distinguish
   * among the different threads, a counter is appended to the name for
   * each thread).
   *
   * @param daemon if <code>true</code>, the threads created by this pool will
   * be set as daemon.
   *
   * @param maxSize the maximum number of threads that this pool creates.
   */
  protected ThreadPool(String name, boolean daemon, int maxSize) {
    super(maxSize);
    this.name     = name;
    this.daemon   = daemon;
  }

  /**
   * Creates a thread pool.
   *
   * @param name the name of the threads that will be created (to distinguish
   * among the different threads, a counter is appended to the name for
   * each thread).
   *
   * @param daemon if <code>true</code>, the threads created by this pool will
   * be set as daemon.
   */
  protected ThreadPool(String name, boolean daemon) {
    this.name     = name;
    this.daemon   = daemon;
  }  

  /**
   * @see org.sapia.ubik.util.pool.Pool#onAcquire(Object)
   *
   * @throws IllegalStateException if this instance is shutting down or is shut down.
   */
  protected PooledThread<T> onAcquire(PooledThread<T> o) throws Exception, IllegalStateException {
    if (shuttingDown) {
      throw new IllegalStateException(
        "Could not acquire thread; pool is shutting down");
    }

    o.acquire();
    busy.add(o);

    return o;
  }

  /**
   * @see org.sapia.ubik.util.pool.Pool#onRelease(Object)
   */
  protected synchronized void onRelease(PooledThread<T> thread) {
    if(shuttingDown) {
      thread.shutdown();
      notifyAll();
    } else {
      thread.release();
      busy.remove(thread);
      notifyAll();
    }
  }
  

  /**
   * Cleanly shuts down this instance.
   *
   * @see #shutdown(long)
   */
  public synchronized void shutdown() {
    shutdown(0);
  }
  
  /**
   * @return the number of active threads in this instance.
   */
  public int getThreadCount(){
    return busy.size();
  }

  /**
   * Cleanly shuts down this instance; internally busy threads
   * are interrupted - currently executing threads finish their
   * task before termination.
   * <p>
   * This method waits that all threads are finished before it
   * returns, OR until the given timeout is reached.
   *
   * @param timeout a shutdown timeout - this method will return
   * when this timeout is reached, even if some threads are still
   * executing.
   */
  public synchronized void shutdown(long timeout) {
     shuttingDown = true;

    for (int i = 0; i < objects.size(); i++) {
      objects.get(i).shutdown();
    }

    if (busy.size() > 0) {
      for (int i = 0; i < busy.size(); i++) {
        busy.get(i).shutdown();
      }

      Delay timer = new Delay(timeout);

      while (busy.size() != 0 && !timer.isOver()) {
        try {
          wait(timer.remainingNotZero());
        } catch (InterruptedException e) {
          return;
        }
      }
    }
  }

  /**
   * @see org.sapia.ubik.util.pool.Pool#doNewObject()
   */
  protected final PooledThread<T> doNewObject() throws Exception {
    PooledThread<T> th = newThread(name + "-" + super.getCreatedCount());
    th.setOwner(this);
    th.setDaemon(daemon);
    th.start();

    return th;
  }

  /**
   * This method must be overridden by inheriting classes; the returned
   * thread must not be started by this method; the pool implements this
   * behavior.
   *
   * @return a {@link PooledThread} instance.
   */
  protected abstract PooledThread<T> newThread(String name) throws Exception;
}
