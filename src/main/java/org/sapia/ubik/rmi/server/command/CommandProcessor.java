package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;


/**
 * An instance of this class serves as an entry-point for command
 * objects, which are processed either synchronously ({@link #processSyncCommand(Command)})
 * or asynchronously ({@link #processAsyncCommand(String, VmId, ServerAddress, Command)}).
 *
 * @author Yanick Duchesne
 */
public class CommandProcessor {
  
  private Category                log = Log.createCategory(getClass());
  private ExecQueue<AsyncCommand> in;

  CommandProcessor(int maxThreads, OutqueueManager outqueues) {
    in = new InQueue(maxThreads,  outqueues);
  }

  /**
   * Processes this command in the same thread as the caller's.
   *
   * @param cmd a {@link Command}.
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
   * Processes the given command asynchronously. The method internally creates an {@link AsyncCommand} instance,
   * which is dispatched to an {@link InQueue} instance.
   *
   * @param cmdId a command's unique identifier.
   * @param from the {@link ServerAddress} from which this command originates.
   * @param cmd the command to execute.
   * 
   * @see InQueue
   */
  public void processAsyncCommand(long cmdId, VmId caller, ServerAddress from, Command cmd) {
    in.add(new AsyncCommand(cmdId, caller, from, cmd));
  }

  /**
   * Shuts down this instance. Blocks until no commands are left to execute,
   * or until the given timeout is reached.
   *
   * @param timeout a timout, in millis.
   * @throws InterruptedException if the calling thread is interrupted while
   * blocking within this method.
   */
  public void shutdown(long timeout) throws InterruptedException {
    log.info("Shutting down incoming command queue");
    in.shutdown(timeout);
  }
}
