package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.rmi.server.VmId;


/**
 * An instance of this class serves as an entry-point for command
 * objects.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CommandProcessor {
  private ExecQueue<AsyncCommand> _in;

  /**
   * Constructor for CommandProcessor.
   */
  public CommandProcessor(int maxThreads) throws IllegalStateException {
    try {
      _in = new InQueue(maxThreads);
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalStateException(e.getMessage());
    }
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
   * Processes the given command asynchronously.
   *
   * @param cmdId a command's unique identifier.
   * @param from the {@link ServerAddress} from which this command originates.
   * @param cmd the command to execute.
   */
  public void processAsyncCommand(String cmdId, VmId caller,
    ServerAddress from, Command cmd) {
    _in.add(new AsyncCommand(cmdId, caller, from, cmd));
  }

  /**
   * Sets this processor's "response sender". A response sender is, as the name
   * implies, in charge of returing a command's return value to the originator of
   * the command.
   *
   * @param sender a  {@link ResponseSender}.
   */
  public void setResponseSender(ResponseSender sender) {
    OutQueue.setResponseSender(sender);
  }

  /**
   * Shuts down this instance. Blocks until no commands are left to execute,
   * or until the given timeout is reached.
   * <p>
   * This method also internally calls shutdownAll() on the <code>OutQueue</code>
   * singleton.
   *
   * @param timeout a timout, in millis.
   * @throws InterruptedException if the calling thread is interrupted while
   * blocking within this method.
   */
  public void shutdown(long timeout) throws InterruptedException {
    Log.warning(getClass(), "Shutting down incoming command queue");
    _in.shutdown(timeout);
    Log.warning(getClass(), "Shutting down outgoing response queue");
    OutQueue.shutdownAll(timeout);
  }
}
