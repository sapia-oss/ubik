package org.sapia.ubik.net;

import java.util.ArrayList;
import java.util.List;

import org.sapia.ubik.rmi.server.perf.HitStatFactory;
import org.sapia.ubik.rmi.server.perf.HitsPerSecStatistic;
import org.sapia.ubik.rmi.server.perf.Statistic;


/**
 * A pool of <code>PooledThread</code> instances. Inheriting classes
 * must implement the <code>newThread()</code> method, which must return
 * an application-specific <code>PooledThread</code> instance.
 * <p>
 * Applications must use the pool in the following manner:
 *
 * <pre>
 *
 * PooledThread thread = (PooledThread)threadPool.acquire();
 *
 * thread.exec(someData);
 *
 * </pre>
 *
 * Upon the <code>exec()</code> method being called, the thread
 * immediately:
 * <ul>
 * <li>calls its own <code>doExec</code> method;
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
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class ThreadPool extends Pool<PooledThread> {
  private String  _name;
  private boolean _daemon;
  private boolean _shuttingDown;
  private List<PooledThread>    _busy = new ArrayList<PooledThread>();
  private HitsPerSecStatistic _tps = HitStatFactory.createHitsPerSec("TPC", -0, null);
  private Statistic    _duration = new Statistic("Duration");  

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
    _name     = name;
    _daemon   = daemon;
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
    _name     = name;
    _daemon   = daemon;
  }  

  /**
   * @see org.sapia.ubik.net.Pool#onAcquire(Object)
   *
   * @throws IllegalStateException if this instance is shutting down or is shut down.
   */
  protected PooledThread onAcquire(PooledThread o) throws Exception, IllegalStateException {
    if (_shuttingDown) {
      throw new IllegalStateException(
        "Could not acquire thread; pool is shutting down");
    }

    o.acquire();
    _busy.add(o);

    return o;
  }

  /**
   * @see org.sapia.ubik.net.Pool#onRelease(Object)
   */
  protected synchronized void onRelease(PooledThread o) {
    ((PooledThread) o).release();
    _busy.remove(o);
    notifyAll();
  }
  
  /**
   * @return this instance's requests-per-second stat.
   */
  public Statistic getRpsStat(){
    return _tps;
  }
  
  /**
   * @return
   */
  public Statistic getDurationStat(){
    return _duration;
  }
  
  /**
   * Enables statistics
   */
  public void enabledStats(){
    _tps.setEnabled(true);
    _duration.setEnabled(true);
  }
  
  /**
   * Disables statistics
   */
  public void disableStats(){
    _tps.setEnabled(false);
    _duration.setEnabled(false);
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
    return _busy.size();
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
    _shuttingDown = true;

    for (int i = 0; i < _objects.size(); i++) {
      _objects.get(i).shutdown();
    }

    if (_busy.size() > 0) {
      for (int i = 0; i < _busy.size(); i++) {
        _busy.get(i).shutdown();
      }

      Timer timer = new Timer(timeout);

      while (_busy.size() != 0) {
        try {
          wait(timeout);

          if (timer.isOver()) {
            break;
          }
        } catch (InterruptedException e) {
          return;
        }
      }
    }
  }

  /**
   * @see org.sapia.ubik.net.Pool#doNewObject()
   */
  protected final PooledThread doNewObject() throws Exception {
    PooledThread th = newThread();
    th.setTpsStat(_tps);
    th.setDurationStat(_duration);
    th.setOwner(this);
    th.setName("[" + _name + " - " + super.getCreatedCount() + "]");
    th.setDaemon(_daemon);
    th.start();

    return th;
  }

  /**
   * This method must be overridden by inheriting classes; the returned
   * thread must not be started by this method; the pool implements this
   * behavior.
   *
   * @return a <code>PooledThread</code> instance.
   */
  protected abstract PooledThread newThread() throws Exception;
}
