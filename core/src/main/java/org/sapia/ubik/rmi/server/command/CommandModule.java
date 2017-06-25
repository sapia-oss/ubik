package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Assertions;

/**
 * Encapsulates components pertaining to command execution logic - including the
 * execution callbacks.
 * 
 * @author yduchesne
 * 
 */
public class CommandModule implements Module {

  private CallbackResponseQueue responseQueue;
  private CommandProcessor commandProcessor;

  @Override
  public void init(ModuleContext context) {
    responseQueue    = new CallbackResponseQueue();
  }

  @Override
  public void start(ModuleContext context) {
    commandProcessor = new CommandProcessor(
        context.lookup(TransportManager.class), 
        responseQueue, 
        Threads.getGlobalWorkerPool(), 
        Threads.getGlobalIoOutboundPool()
    );
  }

  @Override
  public void stop() {
    // noop
  }
  
  /**
   * @return the {@link CallbackResponseQueue}.
   */
  public CallbackResponseQueue getCallbackResponseQueue() {
    return responseQueue;
  }

  /**
   * @return the {@link CommandProcessor}.
   */
  public CommandProcessor getCommandProcessor() {
    Assertions.illegalState(commandProcessor == null, "Module not yet started: command processor not available");
    return commandProcessor;
  }

}
