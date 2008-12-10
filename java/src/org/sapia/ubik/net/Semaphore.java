package org.sapia.ubik.net;


/**
 * This class implements a simple semaphore.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Semaphore {
  public static final int NO_MAX     = 0;
  private int             _maxThread = NO_MAX;
  private int             _count     = 0;
  private ThreadFactory   _fac       = new DefaultThreadFactory();

  /**
   * Creates a new instance of this class.
   *
   * @param maxThread the number of threads this instance will be
   * allowed to create.
   */
  public Semaphore(int maxThread) {
    _maxThread = maxThread;
  }

  /**
   * Creates a new instance of this class.
   *
   * @param maxThread the number of threads this instance will be
   * allowed to create.
   * @param fac a <code>ThreadFactory</code> instance.
   */
  public Semaphore(int maxThread, ThreadFactory fac) {
    this(maxThread);
    _fac = fac;
  }

  /**
   * Called when a thread has completed its work.
   */
  synchronized void release() {
    _count = (--_count < 0) ? 0 : _count;
  }

  /**
   * Gets the number of currently running threads that have been
   * borrowed from this instance.
   *
   * @return the number of currently running threads.
   */
  public synchronized int getThreadCount() {
    return _count;
  }

  /**
   * Returns a thread that wraps the given runnable. The returned
   * thread is not yet started.
   * <p>
   * This method uses this semaphore's internal thread factory to create
   * the thread objects - the factory implementation can be specified
   * at semaphore construction time.
   *
   * @return a <code>Thread</code> instance.
   */
  public synchronized Thread acquireFor(Runnable r)
    throws MaxThreadReachedException {
    if ((_maxThread > 0) && (_count >= _maxThread)) {
      throw new MaxThreadReachedException("" + _count);
    }

    _count++;

    return _fac.newThreadFor(new SemaphoreRunnable(this, r));
  }

  /*////////////////////////////////////////////////////////////////////
                             INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  public static final class SemaphoreRunnable implements Runnable {
    private Semaphore _sema;
    private Runnable  _toRun;

    private SemaphoreRunnable(Semaphore s, Runnable toRun) {
      _sema    = s;
      _toRun   = toRun;
    }

    public final void run() {
      _toRun.run();
      _sema.release();
    }
  }

  static final class DefaultThreadFactory implements ThreadFactory {
    /**
     * @see org.sapia.ubik.net.ThreadFactory#newThreadFor(Runnable)
     */
    public Thread newThreadFor(Runnable r) {
      return new Thread(r);
    }
  }
}
