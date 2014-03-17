package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.util.Conf;

/**
 * Encapsulates components pertaining to command execution logic - including the
 * execution callbacks.
 * 
 * @author yduchesne
 * 
 */
public class CommandModule implements Module {

  public static final String COMMAND_MODULE_SHUTDOWN_TIMEOUT = "ubik.rmi.server.module.command.shutdown-timeout";
  private static final int DEFAULT_CALLBACK_THREADS = 5;
  private static final long DEFAULT_SHUTDOWN_TIMEOUT = 10000;

  private CommandProcessor commandProcessor;
  private CallbackResponseQueue callbackResponseQueue;
  private OutqueueManager outqueues;
  private long shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;

  @Override
  public void init(ModuleContext context) {
    callbackResponseQueue = new CallbackResponseQueue();

    int maxOutQueueThreads = Conf.getSystemProperties().getIntProperty(Consts.SERVER_CALLBACK_OUTQUEUE_THREADS,
        OutqueueManager.DEFAULT_OUTQUEUE_THREADS);
    outqueues = new OutqueueManager(context.lookup(TransportManager.class), callbackResponseQueue, maxOutQueueThreads);

    Conf props = Conf.getSystemProperties();
    commandProcessor = new CommandProcessor(props.getIntProperty(Consts.SERVER_CALLBACK_MAX_THREADS, DEFAULT_CALLBACK_THREADS), outqueues);
    shutdownTimeout = props.getLongProperty(COMMAND_MODULE_SHUTDOWN_TIMEOUT, DEFAULT_SHUTDOWN_TIMEOUT);
  }

  @Override
  public void start(ModuleContext context) {
  }

  @Override
  public void stop() {
    callbackResponseQueue.shutdown(shutdownTimeout);
    outqueues.shutdown(shutdownTimeout);
    try {
      commandProcessor.shutdown(shutdownTimeout);
    } catch (InterruptedException e) {
      // noop
    }
  }

  /**
   * @return this instance's {@link CallbackResponseQueue}
   */
  public CallbackResponseQueue getCallbackResponseQueue() {
    return callbackResponseQueue;
  }

  /**
   * @return this instance's {@link CommandProcessor}.
   */
  public CommandProcessor getCommandProcessor() {
    return commandProcessor;
  }

}
