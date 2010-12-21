package org.sapia.ubik.taskman;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.sapia.ubik.rmi.server.Log;

/**
 * An instance of this class executes {@link Task}s in a separate thread: a single
 * thread is shared among multiple tasks.
 * 
 * @author yduchesne
 *
 */
public class DefaultTaskManager implements TaskManager{
  
  public static long DEFAULT_MIN_DELAY = 5000;

  private List<Task> _tasks = new CopyOnWriteArrayList<Task>();
  private List<TaskWrapper> _toAdd = new CopyOnWriteArrayList<TaskWrapper>();
  private List<Task> _processing = new CopyOnWriteArrayList<Task>();
  private long _nextExectime = System.currentTimeMillis();
  private long _minDelay = DEFAULT_MIN_DELAY;
  private Object     _taskLock = new Object();
  private WakeupLock _timerLock = new WakeupLock();
  private Thread     _taskMan;

  public void addTask(TaskContext ctx, Task task) {
    _toAdd.add(new TaskWrapper(ctx, task));
    wakeup();
  }

  public synchronized void shutdown() {
    if (_taskMan != null) {
      while (_taskMan.isAlive()) {
        _taskMan.interrupt();
        try {
          _taskMan.join();
        } catch (InterruptedException e) {
        }
      }
      _taskMan = null;
    }
  }

  void processTasks() {
    _processing.clear();
    if (_toAdd.size() > 0) {
      for(TaskWrapper wrapper:_toAdd){
        _tasks.add(wrapper.task);
      }
      _toAdd.clear();
    }
    synchronized (_taskLock) {
      _processing.addAll(_tasks);
    }
    for (int i = 0; i < _processing.size(); i++) {
      TaskWrapper tw = (TaskWrapper) _processing.get(i);
      if (tw.ctx.isDue()) {
        tw.execute();
      }
    }
    synchronized (_taskLock) {
      calcNextExecTime();
    }
  }

  void calcNextExecTime() {
    synchronized (_taskLock) {
      removeAborted();
      long tmpTime = 0;
      for (int i = 0; i < _tasks.size(); i++) {
        TaskWrapper tw = (TaskWrapper) _tasks.get(i);
        long taskNextExecTime = tw.ctx.getNextExecTime();
        if (tmpTime == 0 || taskNextExecTime < tmpTime) {
          tmpTime = taskNextExecTime;
        }
      }
      _nextExectime = tmpTime;
    }
  }

  void removeAborted() {
    for (int i = 0; i < _tasks.size(); i++) {
      TaskWrapper tw = (TaskWrapper) _tasks.get(i);
      if (tw.ctx.isAborted()) {
        _tasks.remove(i--);
      }
    }
  }

  private synchronized void wakeup() {
    if (_taskMan == null) {
      _taskMan = new Thread(new Runnable() {

        public void run() {
          while (true) {
            try {
              long delay = _nextExectime - System.currentTimeMillis();
              if (delay < 0) {
                delay = 0;
              }
              if(delay < _minDelay){
                delay = _minDelay;
              }
              _timerLock.waitForTask(delay);
            } catch (InterruptedException e) {
              break;
            }
            processTasks();
          }
        }

      }, "Ubik.TaskManager");
      _taskMan.setDaemon(true);
      _taskMan.start();
    }
    _timerLock.wakeup();
  }
  
  ///////////// Inner classes //////////// 

  static class TaskWrapper {

    private TaskContext ctx;

    private Task task;

    TaskWrapper(TaskContext ctx, Task task) {
      this.ctx = ctx;
      this.task = task;
    }

    long execute() {
      try {
        task.exec(ctx);
      } catch (RuntimeException e) {
        Log.error(DefaultTaskManager.class.getName(), e);
      }
      return ctx.calcNextExecTime();
    }

  }

  static class WakeupLock {

    boolean wakeup = false;

    synchronized void waitForTask(long timeout) throws InterruptedException {
      while (!wakeup) {
        if (timeout == 0) {
          break;
        } else {
          super.wait(timeout);
          break;
        }
      }
      wakeup = false;
    }

    synchronized void wakeup() {
      wakeup = true;
      super.notify();
    }
  }

}
