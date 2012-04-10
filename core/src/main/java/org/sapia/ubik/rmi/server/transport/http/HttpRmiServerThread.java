package org.sapia.ubik.rmi.server.transport.http;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.CommandHandler;


/**
 * An instance of this class executes Ubik RMI commands.
 *
 * @author Yanick Duchesne
 */
class HttpRmiServerThread extends PooledThread<Request> {
  
  private Category       log      = Log.createCategory(getClass());
  private CommandHandler handler;
  
  public HttpRmiServerThread(String name) {
    super(name);
    handler = new CommandHandler(getClass());
  }

  /**
   * @see org.sapia.ubik.net.PooledThread#doExec(java.lang.Object)
   */
  protected void doExec(Request req) {
    log.debug("Handling request");
    
    RMICommand    cmd;

    try {
      cmd = (RMICommand) req.getConnection().receive();
    } catch (Exception e) {
      log.error("Could not handle request", e);
      return ;
    }      
    
    log.debug("Command received: %s from %s@%s", 
        cmd.getClass().getName(), 
        req.getConnection().getServerAddress(), 
        cmd.getVmId());

    cmd.init(new Config(req.getServerAddress(), req.getConnection()));
    
    handler.handleCommand(cmd, req.getConnection());

  }

  @Override
  protected void handleExecutionException(Exception e) {
    Log.warning(getClass(), "Error executing thread", e);
  }
  
  
}
