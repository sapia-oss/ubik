package org.sapia.ubik.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to fork ad-hoc threads.
 *
 * @author yduchesne
 *
 */
public class Spawn {
  
  private static final long DEFAULT_KEEP_ALIVE = 30000;
  private static final int  DEFAULT_CORE_POOL_SIZE = 5;
  private static final int  DEFAULT_MAX_POOL_SIZE  = 10;
  private static final int  DEFAULT_QUEUE_SIZE = 100;
  
  private static ThreadPoolExecutor executor;
  
  static {
    ThreadFactory factory = NamedThreadFactory
        .createWith("Spawned")
        .setDaemon(true);

    executor = new ThreadPoolExecutor(
        DEFAULT_CORE_POOL_SIZE, 
        DEFAULT_MAX_POOL_SIZE, 
        DEFAULT_KEEP_ALIVE, 
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(DEFAULT_QUEUE_SIZE)
    );
    
    executor.setThreadFactory(factory);
    executor.allowsCoreThreadTimeOut();
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        executor.shutdown();
      }
    });
  }
  
  private Spawn() {
  }
  

  /**
   * @param runnable a {@link Runnable} to run in a new thread.
   */
  public static void run(Runnable runnable) {
    executor.submit(runnable);
  }
}
