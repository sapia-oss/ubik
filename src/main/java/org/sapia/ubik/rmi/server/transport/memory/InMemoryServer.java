package org.sapia.ubik.rmi.server.transport.memory;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.CommandHandler;

/**
 * Implements an in-memory {@link Server}.
 * 
 * @author yduchesne
 *
 */
public class InMemoryServer implements Server {
  
  private InMemoryAddress serverAddress;
  private ExecutorService requestProcessor = Executors.newFixedThreadPool(10);
  private CommandHandler  handler          = new CommandHandler(getClass());
  
  public InMemoryServer(InMemoryAddress serverAddress) {
    this.serverAddress  = serverAddress;
  }
  
  @Override
  public ServerAddress getServerAddress() {
    return serverAddress;
  }
  
  @Override
  public void start() throws RemoteException {
    
  }
  
  @Override
  public void close() {
    requestProcessor.shutdown();
  }
  
  void handle(final InMemoryRequest request) {
    
    final Object data;
    
    try {
      data = request.getData();
    } catch (Exception e) {
      Log.error(getClass(), "Could read response data", e);
      try {
        request.setResponse(e);
      } catch (Exception e2) {
        Log.error(getClass(), "Could send back error data", e);
      }
      return;
    } 
    
    if(data instanceof RMICommand) {
      
      requestProcessor.execute(new Runnable() {
        
        @Override
        public void run() {
          RMICommand cmd = (RMICommand) data;
          InMemoryResponseConnection connection = new InMemoryResponseConnection(serverAddress, request);
          cmd.init(new Config(serverAddress, connection));
          handler.handleCommand(cmd, connection);
        }
      });
      
    } else {
      try {
        request.setResponse(new IllegalArgumentException("Expected RMICommand instance, got " + data));
      } catch (Exception e) {
        Log.error(getClass(), "Could not set response", e);
      }
    }
    
  }

}
