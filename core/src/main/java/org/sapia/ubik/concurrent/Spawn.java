package org.sapia.ubik.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.TimeValue;

/**
 * Utility class to fork ad-hoc threads.
 *
 * @author yduchesne
 *
 */
public class Spawn {

  private static ThreadPoolExecutor executor;

  static {
    ThreadFactory factory = NamedThreadFactory
        .createWith("Spawned")
        .setDaemon(true);

    Conf conf = Conf.getSystemProperties();
    TimeValue keepAlive = conf.getTimeProperty(Consts.SPAWN_THREADS_KEEP_ALIVE, Defaults.DEFAULT_SPAWN_KEEP_ALIVE);
    executor = new ThreadPoolExecutor(
        conf.getIntProperty(Consts.SPAWN_CORE_THREADS, Defaults.DEFAULT_SPAWN_CORE_POOL_SIZE),
        conf.getIntProperty(Consts.SPAWN_MAX_THREADS, Defaults.DEFAULT_SPAWN_MAX_POOL_SIZE),
        keepAlive.getValue(),
        keepAlive.getUnit(), new ArrayBlockingQueue<Runnable>(
            conf.getIntProperty(Consts.SPAWN_THREADS_QUEUE_SIZE, Defaults.DEFAULT_SPAWN_QUEUE_SIZE)
        )
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
