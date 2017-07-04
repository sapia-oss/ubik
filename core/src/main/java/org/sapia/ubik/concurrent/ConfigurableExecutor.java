package org.sapia.ubik.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.TimeValue;

/**
 * A {@link ThreadPoolExecutor} which is configured with a
 * {@link ThreadingConfiguration}.
 * 
 * @author yduchesne
 * 
 */
public class ConfigurableExecutor extends ThreadPoolExecutor {

  /**
   * An instance of this class is used to configure a
   * {@link ConfigurableExecutor} instance.
   */
  public static class ThreadingConfiguration {

    private int corePoolSize    = Defaults.DEFAULT_WORKER_MAX_POOL_SIZE;
    private int maxPoolSize     = Defaults.DEFAULT_WORKER_MAX_POOL_SIZE;
    private int queueSize       = Defaults.DEFAULT_WORKER_QUEUE_SIZE;
    private TimeValue keepAlive = Defaults.DEFAULT_WORKER_KEEP_ALIVE;
    private RejectedExecutionHandler rejectionHandler = new ThreadPoolExecutor.AbortPolicy();

    /**
     * @param corePoolSize
     *          the core pool size (defaults to 5).
     * @return this instance.
     */
    public ThreadingConfiguration setCorePoolSize(int corePoolSize) {
      this.corePoolSize = corePoolSize;
      return this;
    }
    
    /**
     * @return this instance's core pool size.
     */
    public int getCorePoolSize() {
      return corePoolSize;
    }

    /**
     * @param maxPoolSize
     *          the max pool size (defaults to 25).
     * @return this instance.
     */
    public ThreadingConfiguration setMaxPoolSize(int maxPoolSize) {
      this.maxPoolSize = maxPoolSize;
      return this;
    }
    
    /**
     * @return this instance's max pool size.
     */
    public int getMaxPoolSize() {
      return maxPoolSize;
    }

    /**
     * @param keepValive
     *          the keep-alive {@link TimeValue} of idle threads.
     * @return
     */
    public ThreadingConfiguration setKeepAlive(TimeValue keepAlive) {
      this.keepAlive = keepAlive;
      return this;
    }

    /**
     * @return this instance keep-alive {@link TimeValue}.
     */
    public TimeValue getKeepAlive() {
      return keepAlive;
    }

    /**
     * @param queueSize
     *          the task queue size (defaults to 50).
     * @return this instance.
     */
    public ThreadingConfiguration setQueueSize(int queueSize) {
      this.queueSize = queueSize;
      return this;
    }
    
    /**
     * @return The queue size of this configuration.
     */
    public int getQueueSize() {
      return this.queueSize;
    }
    
    /**
     * @param handler the {@link RejectedExecutionHandler} to use.
     * @return this instance.
     */
    public ThreadingConfiguration setRejectionHandler(RejectedExecutionHandler handler) {
      this.rejectionHandler = handler;
      return this;
    }

    /**
     * @return a new instance of this class.
     */
    public static ThreadingConfiguration newInstance() {
      return new ThreadingConfiguration();
    }

    @Override
    public String toString() {
      return Strings.toStringFor(this, "coreThreads", corePoolSize, "maxThreads", maxPoolSize, "queueSize", queueSize, "keepAlive", keepAlive);
    }
  }

  // ==========================================================================
  
  /**
   * @param conf
   *          a {@link ThreadingConfiguration}.
   */
  public ConfigurableExecutor(ThreadingConfiguration conf) {
    super(conf.corePoolSize, conf.maxPoolSize, conf.keepAlive.getValue(), conf.keepAlive.getUnit(), new ArrayBlockingQueue<Runnable>(conf.queueSize), conf.rejectionHandler);
  }

  /**
   * @param conf
   *          a {@link ThreadingConfiguration}.
   * @param threads
   *          a {@link ThreadFactory}.
   */
  public ConfigurableExecutor(ThreadingConfiguration conf, ThreadFactory threads) {
    super(conf.corePoolSize, conf.maxPoolSize, conf.keepAlive.getValue(), conf.keepAlive.getUnit(), new ArrayBlockingQueue<Runnable>(conf.queueSize),
        threads, conf.rejectionHandler);
  }
}
