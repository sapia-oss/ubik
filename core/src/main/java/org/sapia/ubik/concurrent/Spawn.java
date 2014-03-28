package org.sapia.ubik.concurrent;

/**
 * Utility class to fork ad-hoc threads.
 *
 * @author yduchesne
 *
 */
public class Spawn {

  private Spawn() {
  }

  /**
   * @param runnable a {@link Runnable} to run in a new thread.
   */
  public static void run(Runnable runnable) {
    NamedThreadFactory.createWith("Spawned")
      .setDaemon(true)
      .newThread(runnable)
      .start();
  }
}
