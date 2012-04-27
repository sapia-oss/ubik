package org.sapia.ubik.net;

/**
 * Handles work dispatche by a {@link WorkerPool}.
 *
 * @author Yanick Duchesne
 */
public interface Worker<W> {
  
  /**
   * Executes the "work" passed in, which is an arbitrary
   * application-specific unit of work that this method performs. In
   * fact, it will probably be, in most cases, some data on which this
   * method's implementation will perform some actions. If the
   * object passed in is eventually shared between multiple threads,
   * it should provide a proper thread-safe behavior.
   *
   * @param work some work to execute, or data on which this method should act.
   */
  public void execute(W work);
  
}
