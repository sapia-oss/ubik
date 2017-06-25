package org.sapia.ubik.rmi.server.command;

import java.rmi.dgc.VMID;
import java.util.concurrent.ExecutorService;

import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportManager;

/**
 * An instance of this class serves as an entry-point for command objects, which
 * are processed either synchronously ({@link #processSyncCommand(Command)}) or
 * asynchronously (
 * {@link #processAsyncCommand(String, VmId, ServerAddress, Command)}).
 * 
 * @author yduchesne
 */
public class CommandProcessor {

  private Category log = Log.createCategory(getClass());
  
  private TransportManager      transports;
  private CallbackResponseQueue localResponseQueue;
  private ExecutorService       asyncExecutor;
  private ExecutorService       outboundExecutor;

  private Stopwatch commandExecTime         = Stats.createStopwatch(getClass(), "AsyncCommandExecTime", "Async command execution time");
  private Stopwatch commandResponseSendTime = Stats.createStopwatch(getClass(), "AsyncCommandResponseSendTime", "Async command response send time");
  
  CommandProcessor(
      TransportManager transports, 
      CallbackResponseQueue localResponseQueue, 
      ExecutorService asyncExecutor, 
      ExecutorService outboundExecutor) {
    this.transports         = transports;
    this.localResponseQueue = localResponseQueue;
    this.asyncExecutor      = asyncExecutor;
    this.outboundExecutor   = outboundExecutor;
  }

  /**
   * Processes this command in the same thread as the caller's.
   * 
   * @param cmd
   *          a {@link Command}.
   * @return the passed in command return value.
   */
  public Object processSyncCommand(Command cmd) {
    try {
      return cmd.execute();
    } catch (Throwable t) {
      return t;
    }
  }

  /**
   * Processes the given command asynchronously. The method internally creates
   * an {@link AsyncCommand} instance, which is dispatched to an {@link InQueue}
   * instance.
   * 
   * @param cmdId
   *          a command's unique identifier.
   * @param from
   *          the {@link ServerAddress} from which this command originates.
   * @param cmd
   *          the command to execute.
   * 
   * @see InQueue
   */
  public void processAsyncCommand(long cmdId, VmId caller, ServerAddress from, Command cmd) {
    asyncExecutor.submit(new Runnable() {
      @Override
      public void run() {
        AsyncCommand async = new AsyncCommand(cmdId, caller, from, cmd);
        Object toReturn = null;

        Split split = commandExecTime.start();
        try {
          toReturn = async.execute();
        } catch (Throwable t) {
          toReturn = t;
        } finally {
          split.stop();
        }
        
        if (async.getCallerVmId().equals(VmId.getInstance())) {
          localResponseQueue.onResponse(new Response(async.getCmdId(), toReturn));
        } else {
          doSendResponse(new Destination(async.getFrom(), async.getCallerVmId()), new Response(async.getCmdId(), toReturn));
        }
      }
    });
  }
  
  private void doSendResponse(Destination dest, Response resp) {
    
    outboundExecutor.submit(new Runnable() {
      
      @Override
      public void run() {
        Split split = commandResponseSendTime.start();
        try {
          doRun();
        } finally {
          split.stop();
        }
        
      }
      
      private void doRun() {
        RmiConnection conn = null;
        Connections pool = null;

        // sending
        try {
          pool = transports.getConnectionsFor(dest.getServerAddress());
          conn = pool.acquire();
          conn.send(new CallbackResponseCommand(resp), dest.getVmId(), dest.getServerAddress().getTransportType());
        } catch (Exception e) {
          log.error("Error sending command to %s", e, dest.getServerAddress());
          if (pool != null && conn != null) {
            pool.invalidate(conn);
            pool.clear();
          }
          return;
        }

        // receiving ack
        try {
          conn.receive();
          pool.release(conn);
        } catch (Exception e) {
          log.info("Error receiving ack from %s", e, dest.getServerAddress());
          pool.invalidate(conn);
          pool.clear();
        }
      }
    });
    
  }
}
