package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.CommandHandler;

/**
 * Implements a thread in a {@link SocketRmiServer} instance.
 *
 * @author Yanick Duchesne
 */
public class SocketRmiServerThread extends PooledThread<Request> {

  private Category       log      = Log.createCategory(getClass());
  private Connection     current;
  private CommandHandler handler;

  SocketRmiServerThread(String name) {
    super(name);
    handler = new CommandHandler(getClass());
  }

  /**
   * @see java.lang.Thread#interrupt()
   */
  public void interrupt() {
    super.interrupt();

    if (current != null) {
      current.close();
    }
  }

  /**
   * @see org.sapia.ubik.net.PooledThread#doExec(Object)
   */
  protected void doExec(Request req) {
    log.debug("Handling request");
    
    current = req.getConnection();

    RMICommand cmd;

    while (true) {
      try {
        cmd = (RMICommand) req.getConnection().receive();
      } catch (RemoteException e) {
        log.warning("Caught remote exception: client probably closed the connection. Exiting thread");
        break;
      } catch (IOException e) {
        log.warning("Caught IO exception: client might have closed the connection. Exiting thread");
        req.getConnection().close();
        break;
      } catch (Exception e) {
        log.error("Could not handle request", e);
        continue;
      }

      log.debug("Command received: %s from %s@%s", 
                cmd.getClass().getName(), 
                req.getConnection().getServerAddress(), 
                cmd.getVmId());
      
      cmd.init(new Config(req.getServerAddress(), req.getConnection()));
      
      handler.handleCommand(cmd, req.getConnection());
    }
  }
  
  @Override
  protected void handleExecutionException(Exception e) {
    Log.warning(getClass(), "Error executing thread", e);
  }
  
}
