package org.sapia.ubik.net;

import org.sapia.ubik.util.pool.Pool;


/**
 * Implements a pooled thread. Inheriting classes need only implementing
 * the {@link #doExec(Object)} template method.
 *
 * @see ThreadPool
 * @see #doExec(Object)
 *
 * @author Yanick Duchesne
 */
public abstract class PooledThread<T> extends Thread {
  
  private Pool<PooledThread<T>> pool;
  private T                     task;
  private volatile boolean      shutdown;
  private boolean               aquired;

  protected PooledThread(String name) {
    super(name);
  }
  

  /** Sets the pool that owns this thread */
  final void setOwner(Pool<PooledThread<T>> pool) {
    this.pool = pool;
  }

  /**
   * Sets this pool's task, which represents a unit of work to
   * perform. This wakes up the thread, which then calls
   * its own {@link #doExec(Object)} method.
   */
  public final synchronized void exec(T task) {
    this.task = task;
    notify();
  }

  /**
   * Stops this thread.
   */
  public void shutdown() {
    shutdown = true;
    interrupt();
  }

  /**
   * @return <code>true</code> if this instance's {@link #shutdown()} method has been invoked.
   */
  boolean isShutdown() {
    return shutdown;
  }

  void acquire() {
    aquired = true;
  }

  void release() {
    aquired = false;
  }

  public final synchronized void run() {
    while (true) {
      while ((task == null) && !shutdown) {
        try {
          wait();
        } catch (InterruptedException e) {
          task = null;

          if (aquired) {
            pool.release(this);

            return;
          }
        }
      }

      if (shutdown) {
        task = null;

        if (aquired) {
          pool.release(this);
        }

      } else {
      
        try {
          doExec(task);
        } catch (Exception e) {
          handleExecutionException(e);
        } 
        
        task = null;
        pool.release(this);
      }
    }
  }

  /**
   * Executes the "task" passed in, which is an arbitrary
   * application-specific unit of work that this method performs. In
   * fact, it will probably be, in most cases, some data on which this
   * method's implementation will perform some actions. If the
   * object passed in is eventually shared between multiple threads,
   * it should provide a proper thread-safe behavior.
   * <p>
   * This template method is to be implemented by subclasses.
   *
   * @param task a task to execute, or data on which this method should act.
   */
  protected abstract void doExec(T task);
  
  /**
   * Internally called from {@link #run()} when an exception is thrown when invoking the {@link #doExec(Object)}
   * method.
   * 
   * @param e the {@link Exception} that was thrown from {@link #doExec(Object)} and internally caught.
   */
  protected abstract void handleExecutionException(Exception e);
}
