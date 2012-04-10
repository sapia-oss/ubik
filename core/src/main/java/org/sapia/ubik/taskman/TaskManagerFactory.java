package org.sapia.ubik.taskman;

/**
 * A factory of {@link TaskManager}s.
 * 
 * @author yduchesne
 *
 */
public class TaskManagerFactory {
  
  /**
   * @return a default {@link TaskManager}
   */
  public static TaskManager createDefaulTaskManager(){
    return createMultithreadedTaskManager();
  }
  
  /**
   * @return a {@link TaskManager} that executes all tasks in
   * separate threads.
   */
  public static TaskManager createMultithreadedTaskManager(){
    return new MultiThreadedTaskManager();
  }
  
}
