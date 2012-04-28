package org.sapia.ubik.rmi.server.transport;

import java.rmi.RemoteException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.stats.Hits;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.stats.Timer;

/**
 * Utility class that handles incoming {@link RMICommand} instances on the server-side.
 * 
 * @author yduchesne
 *
 */
public class CommandHandler {
  
  private Category log;
  private Timer    remoteCall;
  private Timer    sendResponse;
  private Timer    execTime;
  private Hits     tps;  

  /**
   * @param baseName the base name used to create this instance's statistics. 
   */
  public CommandHandler(Class<?> owner) {
    log          = Log.createCategory(owner);
   
    remoteCall   = Stats.getInstance().createTimer(
                     owner, 
                     "RemoteCall", 
                     "Avg time for processing a remote call");
    
    sendResponse = Stats.getInstance().createTimer(
                     owner, 
                     "SendResponse", 
                     "Avg time for sending the return value of a remote call");
    
    execTime     = Stats.getInstance().createTimer(
                     owner, 
                     "ExecutionTime", 
                     "Total avg time for processing a remote call and sending its return value");    
    
    tps          = Stats.getInstance().getHitsBuilder(
                     owner, 
                     "HitsPerSecond", 
                     "The number of hits per second")
                     .perSecond().build();
  }
  
  public void handleCommand(RMICommand cmd, Connection client) {
    execTime.start();
    tps.hit();    
    try {
      doHandleCommand(cmd, client);
    } finally {
      execTime.end();      
    }
  }
  
  private void doHandleCommand(RMICommand cmd, Connection client) {
   
    Object     resp = null;

    
    boolean invokeCommand = cmd instanceof InvokeCommand;
    
    if(invokeCommand && log.isDebug()) {
      log.debug("Performing method invocation: %s", ((InvokeCommand)cmd).getMethodName());
    }
    
    try {
      
      if(invokeCommand) remoteCall.start();
      resp = cmd.execute();
      if(invokeCommand) remoteCall.end();
      
    } catch (Throwable t) {
      resp = t;
    }

    if (invokeCommand) sendResponse.start();

    try {
      ((RmiConnection) client).send(resp, cmd.getVmId(), cmd.getServerAddress().getTransportType());
    } catch (RemoteException e) {
      log.warning("Could not send response; client " + client.getServerAddress()  + " has probably terminated connection");
    } catch (Exception e) {
      log.warning("Exception caught try to send response", e);
      sendErrorResponse(e, client);
    } finally {
      if (invokeCommand) sendResponse.end();
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
