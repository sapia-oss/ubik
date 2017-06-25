package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;

import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportProvider;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.TimeValue;

/**
 * This module is used to send non-remote method invocation commands to other
 * servers in an asynchronous manner, to avoid blocking issues with some
 * {@link TransportProvider} implementations.
 * 
 * @author yduchesne
 * 
 */
public class AsyncCommandSender implements Module {

  private static Stopwatch sendTime = Stats.createStopwatch(AsyncCommandSender.class, "SendDuration", "Time required to send command");

  private Category log = Log.createCategory(getClass());

  private ExecutorService senders;

  @Override
  public void init(ModuleContext context) {

  }

  @Override
  public void start(ModuleContext context) {
    senders = Threads.createIoOutboundPool();
  }

  @Override
  public void stop() {
    senders.shutdown();
  }

  /**
   * Sends the given command asynchronously.
   * 
   * @param command
   *          a {@link RMICommand}.
   * @param endpoint
   *          the {@link ServerAddress} corresponding to the server to which to
   *          send the command.
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
      throw new RemoteException("Could not send: " + command + " to " + endpoint, e);
    } catch (RemoteException e) {
      conns.clear();

      try {
        doSend(conns, command);
      } catch (RemoteException e2) {
        log.warning("Could not send: " + command + " to " + endpoint + "; server probably down");
      } catch (Exception e2) {
        throw new RemoteException("Could not send: " + command + " to " + endpoint, e2);
      }
    } catch (IOException e) {
      throw new RemoteException("Could not send: " + command + " to " + endpoint, e);
    }
  }

  private static void doSend(Connections conns, RMICommand command) throws RemoteException, IOException, ClassNotFoundException {
    RmiConnection conn = null;

    try {
      Split split = sendTime.start();
      conn = conns.acquire();
      conn.send(command);
      conn.receive();
      split.stop();
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
