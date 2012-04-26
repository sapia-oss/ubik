package org.sapia.ubik.rmi.server.invocation;

import java.io.IOException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.module.ModuleContext.State;
import org.sapia.ubik.rmi.server.ClientRuntime;
import org.sapia.ubik.rmi.server.ShutdownException;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.Timer;
import org.sapia.ubik.rmi.server.transport.Connections;

/**
 * This class handles remote method invocations on the client-side.
 *
 * @author Yanick Duchesne
 */
public class InvocationDispatcher implements Module {
  
  public static class InvocationStats {
    Timer acquireCon    = Stats.getInstance().createTimer(
                            InvocationDispatcher.class, 
                            "AcquireConnection", 
                            "Avg time to acquire a connection");
    
    Timer invokeSend    = Stats.getInstance().createTimer(
                            InvocationDispatcher.class, 
                            "InvokeSend",
                            "Avg time to send a remote invocation command");
    
    Timer invokeReceive = Stats.getInstance().createTimer(
                            InvocationDispatcher.class, 
                            "InvokeReceive", 
                            "Avg time to receive the result of a remote invocation");
  }
  
  // --------------------------------------------------------------------------
  
  public interface InvocationStrategy {
    
    public void init(ModuleContext modules);
    
    public Object dispatchInvocation(InvocationStats stats, Connections pool, InvokeCommand cmd)
    throws IOException, 
           ClassNotFoundException, 
           Throwable;
  }

  // --------------------------------------------------------------------------

  private Category                  log              = Log.createCategory(getClass());
  private InvocationStats           stats            = new InvocationStats();
  private ModuleContext             context;
  private ClientRuntime             clientRuntime;
  private InvocationStrategyFactory invocationStrategyFactory;
 
  
  @Override
  public void init(ModuleContext context) {
    this.context              = context;
    clientRuntime             = context.lookup(ClientRuntime.class);
    invocationStrategyFactory = new InvocationStrategyFactory(context);
  }
  @Override
  public void start(ModuleContext context) {
  }

  @Override
  public void stop() {
  }
  
  /**
   * Dispatches the given invocation command using the passed in
   * connection pool (which holds connections to a remote server).
   *
   * @param vmId the {@link VmId} of the stub that is performing the call.
   * @param pool {@link Connections} to the server to call.
   * @param cmd an {@link InvokeCommand} representing the remote
   * method invocation to be performed.
   * @return the return value of the invocation.
   * @throws java.io.IOException if an IO problem occurs while performing this
   * operation.
   * @throws ClassNotFoundException if the class of the invocation's return value
   * could not be found in the deserialization process.
   * @throws Throwable if the invocation is colocated and an exception is throws by the
   * colocated call.
   */
  public Object dispatchInvocation(VmId vmId, Connections pool, InvokeCommand cmd)
    throws java.io.IOException, ClassNotFoundException, Throwable {
    
    if (context.getState() == State.STOPPING) {
      throw new ShutdownException();
    }
    
    Object toReturn;
    
    log.debug("Sending invocation for object: %s on vmId %s", cmd.getOID(), vmId);
    
    // CLIENT pre-invoke event dispatch
    ClientPreInvokeEvent pre = new ClientPreInvokeEvent(cmd);
    clientRuntime.getDispatcher().dispatch(pre);
    
    InvocationStrategy handler = invocationStrategyFactory.getInvocationStrategy(vmId);
    toReturn = handler.dispatchInvocation(stats, pool, cmd);

    // CLIENT post-invoke event dispatch
    ClientPostInvokeEvent post = new ClientPostInvokeEvent(pre.getCommand(), toReturn);
    log.debug("Dispatching post-invocation event");
    clientRuntime.getDispatcher().dispatch(post);
    
    log.debug("Returning invocation response %s", toReturn);
    return toReturn;
  }
}
