package org.sapia.ubik.rmi.server.transport;

import java.rmi.RemoteException;

import org.javasimon.Counter;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.stats.Stats;

/**
 * Utility class that handles incoming {@link RMICommand} instances on the server-side.
 * 
 * @author yduchesne
 *
 */
public class CommandHandler {
  
  private Category        log;
  private MultiDispatcher eventDispatcher;
  private Stopwatch       remoteCall;
  private Stopwatch       sendResponse;
  private Stopwatch       execTime;
  private Counter         tps;  

  public CommandHandler(MultiDispatcher eventDispatcher, Class<?> owner) {
    log          = Log.createCategory(owner);
   
    remoteCall   = Stats.createStopwatch(owner,"RemoteCall", "Time for processing a remote call");
    
    sendResponse = Stats.createStopwatch(owner, "SendResponse", "Time for sending the return value of a remote call");
    
    execTime     = Stats.createStopwatch(owner, "ExecutionTime", "Total time for processing a remote call and sending its return value");    
    
    tps          = Stats.createCounter(owner, "Hits", "The number of hits");
    
    this.eventDispatcher = eventDispatcher;
  }
  
  public void handleCommand(RMICommand cmd, Connection client) {
    Split split = execTime.start();
    tps.increase();    
    try {
      doHandleCommand(cmd, client);
    } finally {
      split.stop();      
    }
  }
  
  private void doHandleCommand(RMICommand cmd, Connection client) {
   
    Object     resp = null;

    
    boolean invokeCommand = cmd instanceof InvokeCommand;
    
    if(invokeCommand && log.isDebug()) {
      log.debug("Performing method invocation: %s", ((InvokeCommand)cmd).getMethodName());
    }
    
    try {
      
      Split invoke = remoteCall.start();
      if(invokeCommand) { 
        remoteCall.start();
        eventDispatcher.dispatch(new IncomingCommandEvent(cmd));
      }
      
      resp = cmd.execute();
      if(invokeCommand) invoke.stop();
      
    } catch (Throwable t) {
      resp = t;
    }

    Split send = null;
    if (invokeCommand) { 
      send = sendResponse.start();
    }

    try {
      ((RmiConnection) client).send(resp, cmd.getVmId(), cmd.getServerAddress().getTransportType());
    } catch (RemoteException e) {
      log.warning("Could not send response; client " + client.getServerAddress()  + " has probably terminated connection");
    } catch (Exception e) {
      log.warning("Exception caught try to send response", e);
      sendErrorResponse(e, client);
    } finally {
      if (invokeCommand) {
        send.stop();
      }
    }
  }
  
  private void sendErrorResponse(Exception error, Connection client) {
    try {
      error.fillInStackTrace();
      client.send(error);
    } catch (Exception e2) {
      client.close();
    }
  }
  
}
