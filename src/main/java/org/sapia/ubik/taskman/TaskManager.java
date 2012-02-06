package org.sapia.ubik.taskman;

public interface TaskManager {

  /**
   * Creates an instance of this class.
   * 
   * @param ctx
   *          the {@link TaskContext} that provides information about the task
   *          to execute.
   * @param task
   *          the {@link Task} to execute.
   */
  public void addTask(TaskContext ctx, Task task);
  
}
