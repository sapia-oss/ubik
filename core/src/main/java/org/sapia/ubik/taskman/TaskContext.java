package org.sapia.ubik.taskman;

/**
 * Holds data about a given {@link Task} instance.
 * 
 * @author yduchesne
 * 
 */
public class TaskContext {

  private String name;
  private long interval;
  private long nextExec;
  private boolean aborted;

  public TaskContext(String name, long interval) {
    this.name = name;
    this.interval = interval;
    this.nextExec = System.currentTimeMillis() + interval;
  }

  /**
   * @return the name of the corresponding task.
   */
  public String getName() {
    return name;
  }

  /**
   * @return the interval (in millis) at which the task must be executed.
   */
  public long getInterval() {
    return interval;
  }

  /**
   * @return the next execution time of the task.
   */
  public long getNextExecTime() {
    return nextExec;
  }

  /**
   * Flags this task so that it is no more executed.
   */
  public void abort() {
    aborted = true;
  }

  boolean isAborted() {
    return aborted;
  }

  boolean isDue() {
    return System.currentTimeMillis() >= nextExec;
  }

  long calcNextExecTime() {
    nextExec = System.currentTimeMillis() + interval;
    return nextExec;
  }
}
