package org.sapia.ubik.taskman;

/**
 * This interface specifies the behavior of tasks that are handled by the
 * {@link TaskManager}
 * 
 * @author yduchesne
 * 
 */
public interface Task {

  /**
   * @param ctx
   *          this instance's {@link TaskContext}
   */
  public void exec(TaskContext ctx);

}
