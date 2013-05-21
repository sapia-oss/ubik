package org.sapia.ubik.rmi.server.command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.ShutdownException;
import org.sapia.ubik.rmi.server.stats.Stats;


/**
 * Implements the queue into which incoming {@link AsyncCommand} instances are inserted before 
 * being processed asynchronously
 * <p>
 * As an {@link AsyncCommand} is enqueued, an available processing thread executes the command. 
 * If no thread is available, then the command sits in the queue until a thread becomes available 
 * - and until the command's turn comes - i.e.: this is a queue and commands are treated in a FIFO fashion.
 * <p>
 * After execution of a command, the resulting response is internally dispatched to the {@link OutQueue}
 * that corresponds to the host (i.e.: {@link Destination}) from which the command originates. The
 * {@link OutqueueManager} is used to acquire the {@link OutQueue} that corresponds to the destination.   
 *
 * @author Yanick Duchesne
 */
public class InQueue extends ExecQueue<AsyncCommand> {

  private Category        log             = Log.createCategory(getClass());
  private ExecutorService pool;
  private OutqueueManager outqueues;
  private Stopwatch       commandExecTime = Stats.createStopwatch(
                                              getClass(), 
                                              "AsyncCommandExecTime", 
                                              "Async command execution time"
                                            );

  /**
   * Creates a new instance of this class with the given number of processor threads.
   *
   * @param maxThreads the maximum number of internal threads created by this queue.
   * @param outqueues the {@link OutqueueManager} that is internally used to acquire the {@link OutQueue} instances
   * to which {@link Response}s are queued, on a per-destination basis.
   */
  InQueue(int maxThreads, OutqueueManager outqueues) {
    pool = Executors.newFixedThreadPool(
        maxThreads, 
        NamedThreadFactory.createWith("ubik.rmi.callback.inqueue.thread").setDaemon(true)
    );
    for (int count = 0; count < maxThreads; count++) {
      pool.execute(new InQueueProcessor());
    }
    this.outqueues = outqueues;
  }

  public void shutdown(long timeout) {
    pool.shutdownNow();
  }

  // --------------------------------------------------------------------------
  
  class InQueueProcessor implements Runnable {
    
    @Override
    public void run() {
      while (true) {
        AsyncCommand async;
        Object       toReturn;

        try {
          async = remove();

          try {
            Split split = commandExecTime.start();
            toReturn = async.execute();
            split.stop();
          } catch (ShutdownException e) {
            log.warning("Shutting down...");

            break;
          } catch (Throwable t) {
            toReturn = t;
          }

          outqueues.getQueueFor(new Destination(async.getFrom(), async.getCallerVmId()))
            .add(new Response(async.getCmdId(), toReturn));
          Thread.yield();
        } catch (InterruptedException e) {
          break;
        }
      }
    }
    
  }
}
