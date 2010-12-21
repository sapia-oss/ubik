package org.sapia.ubik.net;

import org.sapia.ubik.rmi.server.perf.HitsPerSecStatistic;
import org.sapia.ubik.rmi.server.perf.Statistic;


/**
 * Implements a pooled thread. Inheriting classes need only implementing
 * the <code>doExec</code> template method.
 *
 * @see ThreadPool
 * @see #doExec(Object)
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class PooledThread extends Thread {
  private Pool<PooledThread>    _pool;
  private Object                _task;
  private boolean               _shutdown;
  private boolean               _aquired;
  protected HitsPerSecStatistic _tps;
  protected Statistic    _duration;  

  protected PooledThread() {
  }
  
  void setTpsStat(HitsPerSecStatistic stat){
    _tps = stat;
  }
  
  void setDurationStat(Statistic stat){
    _duration = stat;
  }  
  

  /** Sets the pool that owns this thread */
  final void setOwner(Pool<PooledThread> pool) {
    _pool = pool;
  }

  /**
   * Sets this pool's task, which represents a unit of work to
   * perform. This wakes up the thread, which then calls
   * its own <code>doExec</code> method.
   *
   * @see #doExec(Object)
   */
  public final synchronized void exec(Object task) {
    _task = task;
    notify();
  }

  /**
   * Stops this thread.
   */
  public void shutdown() {
    _shutdown = true;
    interrupt();
  }

  boolean isShutdown() {
    return _shutdown;
  }

  void acquire() {
    _aquired = true;
  }

  void release() {
    _aquired = false;
  }

  public final synchronized void run() {
    while (true) {
      while ((_task == null) && !_shutdown) {
        try {
          wait();
        } catch (InterruptedException e) {
          _task = null;

          if (_aquired) {
            _pool.release(this);

            return;
          }
        }
      }

      if (_shutdown) {
        _task = null;

        if (_aquired) {
          _pool.release(this);
        }

        return;
      }
      
      try {
        doExec(_task);
      } catch (Exception e) {
        handleExecutionException(e);
        // noop
      }
      
      _task = null;
      _pool.release(this);
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
   * @param a task to execute, or data on which this method should act.
   */
  protected abstract void doExec(Object task);
  
  protected abstract void handleExecutionException(Exception e);
}
