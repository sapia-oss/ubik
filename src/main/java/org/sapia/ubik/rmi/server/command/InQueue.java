package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.ThreadPool;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.ShutdownException;


/**
 * Implements the queue into which incoming <code>Executable</code>
 * instances are inserted before being processed in separate threads.
 * <p>
 * The <code>Executable</code> instances are in this case expected to be
 * <code>AsyncCommand</code> instances.
 * <p>
 * As an <code>AsyncCommand</code> is enqueued, an available processing thread
 * handles the command is executed. If no thread is available, then the command
 * sits in the queue until a thread becomes available - and until the command's
 * turn comes - i.e.: this is a queue and commands are treated in a FIFO fashion.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class InQueue extends ExecQueue {
  CmdProcessorThreadPool _pool;

  /**
   * Creates a new instance of this clas with one internal processor thread.
   */
  InQueue() throws Exception {
    this(1);
  }

  /**
   * Creates a new instance of this clas with the given number of processor threads.
   *
   * @param maxThreads the maximum number of internal threads created by this queue.
   */
  InQueue(int maxThreads) throws Exception {
    super();

    if (maxThreads <= 0) {
      maxThreads = 1;
    }

    _pool = new CmdProcessorThreadPool(maxThreads);
    _pool.fill(maxThreads);

    PooledThread pt;

    for (int count = 0; count < maxThreads; count++) {
      pt = (PooledThread) _pool.acquire();
      pt.exec(this);
    }
  }

  public void shutdown(long timeout) throws InterruptedException {
    super.shutdown(timeout);
    _pool.shutdown(timeout);
  }

  /*////////////////////////////////////////////////////////////////////
                               INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  static class CmdProcessorThread extends PooledThread {
    /**
     * @see org.sapia.ubik.net.PooledThread#doExec(Object)
     */
    protected void doExec(Object task) {
      InQueue queue = (InQueue) task;

      while (true) {
        AsyncCommand async;
        Object       toReturn;

        try {
          async = (AsyncCommand) queue.remove();

          try {
            toReturn = async.execute();
          } catch (ShutdownException e) {
            Log.warning(getName(), "Shutting down...");

            break;
          } catch (Throwable t) {
            toReturn = t;
          }

          OutQueue.getQueueFor(new Destination(async.getFrom(),
              async.getCallerVmId())).add(new Response(async.getCmdId(),
              toReturn));
          Thread.yield();
        } catch (InterruptedException e) {
          break;
        }
      }
    }
    
    @Override
    protected void handleExecutionException(Exception e) {
      Log.warning(getClass(), "Error executing thread", e);
    }

    /**
     * @see org.sapia.ubik.net.PooledThread#shutdown()
     */
    public void shutdown() {
      Log.warning(getName(), "Shut down signal received...");
      super.shutdown();
    }
  }

  static class CmdProcessorThreadPool extends ThreadPool {
    /**
     * Constructor for CmdProcessorThreadPool.
     * @param name
     * @param maxSize
     */
    public CmdProcessorThreadPool(int maxSize) {
      super("ubik.rmi.CallbackThread", true, maxSize);
    }

    /**
     * @see org.sapia.ubik.net.ThreadPool#newThread()
     */
    protected PooledThread newThread() throws Exception {
      return new CmdProcessorThread();
    }
  }
}
