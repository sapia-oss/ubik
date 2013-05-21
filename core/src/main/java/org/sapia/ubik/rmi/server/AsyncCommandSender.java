package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.rmi.RemoteException;

import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.util.Time;

/**
 * This module is used to send non-remote method invocation commands to other servers
 * in an asynchronous manner, to avoid blocking issues with some {@link TransportProvider} 
 * implementations.
 * 
 * @author yduchesne
 *
 */
public class AsyncCommandSender implements Module {
  
  private static final int DEFAULT_CORE_POOL_SIZE = 5;
  private static final int DEFAULT_MAX_POOL_SIZE  = 5;
  private static final int DEFAULT_QUEUE_SIZE     = 1000;
  private static final Time DEFAULT_KEEP_ALIVE    = Time.createSeconds(30);
  
  private Category log = Log.createCategory(getClass());
  
  private ConfigurableExecutor senders;
  
  @Override
  public void init(ModuleContext context) {
    ThreadingConfiguration conf = ThreadingConfiguration.newInstance()
        .setCorePoolSize(DEFAULT_CORE_POOL_SIZE)
        .setMaxPoolSize(DEFAULT_MAX_POOL_SIZE)
        .setQueueSize(DEFAULT_QUEUE_SIZE)
        .setKeepAlive(DEFAULT_KEEP_ALIVE);
    
    senders = new ConfigurableExecutor(conf, NamedThreadFactory.createWith("ubik.rmi.AsyncSender").setDaemon(true));
    
  }
  
  @Override
  public void start(ModuleContext context) {
  }
  
  @Override
  public void stop() {
    senders.shutdown();
  }
  
  /**
   * Sends the given command asynchronously.
   * 
   * @param command a {@link RMICommand}.
   * @param endpoint the {@link ServerAddress} corresponding to the server to which to send the command.
   */
  public void send(final RMICommand command, final ServerAddress endpoint) {
    senders.submit(new Runnable() {
      @Override
      public void run() {
        try {
          doRun(command, endpoint);
        } catch (Exception e) {
          log.error("Caught error sending command asynchronously", e);
        }
      }
    });
  }
  
  private void doRun(RMICommand command, ServerAddress endpoint) throws RemoteException {
    Connections conns = Hub.getModules().getTransportManager().getConnectionsFor(endpoint);
    try {
      doSend(conns, command);
    } catch (ClassNotFoundException e) {
      throw new RemoteException("Could not send: " + command + " to " +
        endpoint, e);
    } catch (RemoteException e) {
      conns.clear();
  
      try {
        doSend(conns, command);
      } catch (RemoteException e2) {
        log.warning("Could not send: " + command + " to " + 
            endpoint + "; server probably down");
      } catch (Exception e2) {
        throw new RemoteException("Could not send: " + command + " to " +
          endpoint, e2);
      }
    } catch (IOException e) {
      throw new RemoteException("Could not send: " + command + " to " +
        endpoint, e);
    }     
  }
  

  private static void doSend(Connections conns, RMICommand command)
  throws RemoteException, IOException, ClassNotFoundException {
    RmiConnection conn = null;
  
    try {
      conn = conns.acquire();
      conn.send(command);
      conn.receive();
    } catch (Exception e) {
      if (conn != null) {
        conns.invalidate(conn);
      }
    } finally {
      if (conn != null) {
        conns.release(conn);
      }
    }
  }    

}
