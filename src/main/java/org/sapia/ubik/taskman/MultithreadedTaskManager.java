package org.sapia.ubik.taskman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.perf.HitStatFactory;
import org.sapia.ubik.rmi.server.perf.HitsPerMinStatistic;

/**
 * This class implements a {@link TaskManager} that creates a thread for each
 * task added to it.
 * 
 * @author yduchesne
 *
 */
public class MultithreadedTaskManager implements TaskManager{

  private List<Thread> _threads = Collections.synchronizedList(new ArrayList<Thread>());
  
  public void addTask(final TaskContext ctx, final Task task) {
    final HitsPerMinStatistic stat = HitStatFactory.createHitsPerMin("TaskManager."+ctx.getName(), ctx.getInterval(), Hub.statsCollector);
    Thread taskThread = new Thread(
        new Runnable(){
          public void run() {
            while(true){
              try{
                Thread.sleep(ctx.getInterval());
                task.exec(ctx);
                stat.hit();
              }catch(InterruptedException e){
                Log.warning(getClass(), "Interrupted...");
                break;
              }
              if(Thread.interrupted() || ctx.isAborted()){
                _threads.remove(Thread.currentThread());
                break;
              }
            }
          }
        },
        task.getClass().getName()
    );
    taskThread.setDaemon(true);
    taskThread.start();
    _threads.add(taskThread);
  }
  
  public void shutdown() {
    synchronized (_threads) {
      for(int i = 0; i < _threads.size(); i++){
        Thread t = (Thread)_threads.get(i);
        t.interrupt();
        int count = 0;
        while(t.isAlive() && count < 3){
          try{
            t.join(1000);
            t.interrupt();
          }catch(InterruptedException e){
            return;
          }
          count++;
        }
      }
    }
  }
}