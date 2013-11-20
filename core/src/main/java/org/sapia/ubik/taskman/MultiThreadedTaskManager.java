package org.sapia.ubik.taskman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.javasimon.Counter;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.server.stats.Stats;

/**
 * This class implements a {@link TaskManager} that creates a thread for each
 * task added to it.
 * 
 * @author yduchesne
 * 
 */
public class MultiThreadedTaskManager implements TaskManager, Module {

  private Category log = Log.createCategory(getClass());
  private List<Thread> threads = Collections.synchronizedList(new ArrayList<Thread>());

  private Counter taskExecutionPerMinute = Stats.createCounter(getClass(), "Tasks", "Number of task executions");

  private Stopwatch taskExecutionTime = Stats.createStopwatch(getClass(), "TaskExecTime", "Task execution time");

  @Override
  public void init(ModuleContext context) {
  }

  @Override
  public void start(ModuleContext context) {
  }

  @Override
  public void stop() {
    synchronized (threads) {
      for (int i = 0; i < threads.size(); i++) {
        Thread t = threads.get(i);
        try {
          ThreadShutdown.create(t).shutdown();
        } catch (InterruptedException e) {
          break;
        }
      }
    }
  }

  public void addTask(final TaskContext ctx, final Task task) {
    Thread taskThread = new Thread(new Runnable() {
      public void run() {
        while (true) {
          try {
            Thread.sleep(ctx.getInterval());

            taskExecutionPerMinute.increase();
            Split split = taskExecutionTime.start();
            task.exec(ctx);
            split.stop();

          } catch (InterruptedException e) {
            log.warning("Interrupted task: %s", ctx.getName());
            break;
          }
          if (Thread.interrupted() || ctx.isAborted()) {
            threads.remove(Thread.currentThread());
            break;
          }
        }
      }
    }, ctx.getName());
    taskThread.setDaemon(true);
    taskThread.start();
    threads.add(taskThread);
  }
}