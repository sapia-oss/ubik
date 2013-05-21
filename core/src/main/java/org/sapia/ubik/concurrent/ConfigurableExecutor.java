package org.sapia.ubik.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.Time;

/**
 * A {@link ThreadPoolExecutor} which is configured with a {@link ThreadingConfiguration}.
 * 
 * @author yduchesne
 *
 */
public class ConfigurableExecutor extends ThreadPoolExecutor {
  
  /**
   * An instance of this class is used to configure a {@link ConfigurableExecutor} instance.
   */
  public static class ThreadingConfiguration {
    
    public static final int  DEFAULT_CORE_POOL_SIZE = 10;
    public static final int  DEFAULT_MAX_POOL_SIZE  = 25;
    public static final int  DEFAULT_QUEUE_SIZE     = 50;
    public static final Time DEFAULT_KEEP_ALIVE     = new Time(30, TimeUnit.SECONDS);
    
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;
    private int maxPoolSize  = DEFAULT_MAX_POOL_SIZE;
    private int queueSize    = DEFAULT_QUEUE_SIZE;
    private Time keepAlive   = DEFAULT_KEEP_ALIVE;
    
    /**
     * @param corePoolSize the core pool size (defaults to 5).
     * @return this instance.
     */
    public ThreadingConfiguration setCorePoolSize(int corePoolSize) {
      this.corePoolSize = corePoolSize;
      return this;
    }

    /**
     * @param maxPoolSize the max pool size (defaults to 25).
     * @return this instance.
     */
    public ThreadingConfiguration setMaxPoolSize(int maxPoolSize) {
      this.maxPoolSize = maxPoolSize;
      return this;
    }

    /**
     * @param keepValive the keep-alive {@link Time} of idle threads.
     * @return
     */
    public ThreadingConfiguration setKeepAlive(Time keepAlive) {
      this.keepAlive = keepAlive;
      return this;
    }
    
    /**
     * @param queueSize the task queue size (defaults to 50).
     * @return this instance.
     */
    public ThreadingConfiguration setQueueSize(int queueSize) {
      this.queueSize = queueSize;
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
      return Strings.toStringFor(this, 
          "coreThreads", corePoolSize, 
          "maxThreads", maxPoolSize, 
          "queueSize", queueSize, 
          "keepAlive", keepAlive);
    }
  }
  
  // ==========================================================================
  
  /**
   * @param conf a {@link ThreadingConfiguration}.
   */
  public ConfigurableExecutor(ThreadingConfiguration conf) {
   super(
       conf.corePoolSize, 
       conf.maxPoolSize, 
       conf.keepAlive.getValue(), 
       conf.keepAlive.getUnit(), 
       new ArrayBlockingQueue<Runnable>(conf.queueSize));
  }

  /**
   * @param conf a {@link ThreadingConfiguration}.
   * @param threads a {@link ThreadFactory}.
   */
  public ConfigurableExecutor(ThreadingConfiguration conf, ThreadFactory threads) {
   super(
       conf.corePoolSize, 
       conf.maxPoolSize, 
       conf.keepAlive.getValue(), 
       conf.keepAlive.getUnit(), 
       new ArrayBlockingQueue<Runnable>(conf.queueSize),
       threads);
  }
}
