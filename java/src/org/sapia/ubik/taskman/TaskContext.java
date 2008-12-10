package org.sapia.ubik.taskman;

/**
 * Holds data about a given {@link Task} instance.
 * 
 * @author yduchesne
 *
 */
public class TaskContext {
  
  private String _name;
  
  private long _interval;
  private long _nextExec;
  private boolean _aborted;
  
  public TaskContext(String name, long interval){
    _name = name;
    _interval = interval;
    _nextExec = System.currentTimeMillis()+_interval;
  }
  
  /**
   * @return the name of the corresponding task.
   */
  public String getName(){
    return _name;
  }
  
  /**
   * @return the interval (in millis) at which the task must
   * be executed.
   */
  public long getInterval(){
    return _interval;
  }
  
  /**
   * @return the next execution time of the task.
   */
  public long getNextExecTime(){
    return _nextExec;
  }
  
  /**
   * Flags this task so that it is no more executed. 
   */
  public void abort(){
    _aborted = true;
  }

  boolean isAborted(){
    return _aborted;
  }
  
  boolean isDue(){
    return System.currentTimeMillis() >= _nextExec;
  }
  
  long calcNextExecTime(){
    _nextExec = System.currentTimeMillis() + _interval;
    return _nextExec;
  }
}
