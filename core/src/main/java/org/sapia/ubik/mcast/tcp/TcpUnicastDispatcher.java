package org.sapia.ubik.mcast.tcp;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.DefaultUbikServerSocketFactory;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.SocketConnectionFactory;
import org.sapia.ubik.net.SocketServer;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.Worker;
import org.sapia.ubik.net.WorkerPool;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Localhost;

/**
 * Implements a {@link UnicastDispatcher} over standard server sockets. This
 * class is legacy and the NIO implementation is preferred.
 * 
 * @author yduchesne
 * 
 */
public class TcpUnicastDispatcher extends BaseTcpUnicastDispatcher {

  public static final String TRANSPORT_TYPE = "tcp/unicast";

  private TCPUnicastSocketServer socketServer;
  private ServerAddress          address;
  private Thread                 serverThread;
  
  // --------------------------------------------------------------------------
  // UnicastDispatcher interface

  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    if (address == null) {
      Assertions.illegalState(socketServer == null, "Socket server not started");
      
      address = new TCPAddress(doGetTransportType(), socketServer.getAddress(), socketServer.getPort());
      log.debug("Server address for node %s: %s", context().getConsumer().getNode(), address);
    }
    return address;
  }

  // --------------------------------------------------------------------------
  // Inherited abstract methods

  @Override
  protected void doStart() {
    try {
      socketServer = new TCPUnicastSocketServer(Localhost.getPreferredLocalAddress().getHostAddress(), context().getConsumer(), Threads.createWorkerPool());
    } catch (IOException e) {
      throw new IllegalStateException("Could not start server", e);
    }
    if (serverThread != null && serverThread.isAlive()) {
      throw new IllegalStateException("Server already started");
    }

    serverThread = NamedThreadFactory.createWith("tcp.unicast.dispatcher.Server").setDaemon(true).newThread(socketServer);
    serverThread.start();
    try {
      socketServer.waitStarted();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Thread interrupted while waiting for socket server startup", e);
    } catch (Exception e) {
      throw new IllegalStateException("Could not start socket server", e);
    }
  }

  @Override
  protected void doClose() {
    if (socketServer != null) {
      socketServer.close();
    }
    ThreadShutdown.create(serverThread).shutdownLenient();
  }

  @Override
  protected String doGetTransportType() {
    return TRANSPORT_TYPE;
  }

  @Override
  protected ConnectionFactory doGetConnectionFactory() {
    SocketConnectionFactory connections = new SocketConnectionFactory(TRANSPORT_TYPE);
    return connections;
  }

  // ==========================================================================

  class TCPUnicastSocketServer extends SocketServer {

    public TCPUnicastSocketServer(String bindAddress, EventConsumer consumer, ExecutorService executor) throws IOException {
      super(doGetTransportType(), bindAddress, 0, new TCPUnicastThreadPool(consumer, executor), new DefaultUbikServerSocketFactory());
    }

    public TCPUnicastSocketServer(EventConsumer consumer, ExecutorService executor) throws IOException {
      super(doGetTransportType(), 0, new TCPUnicastThreadPool(consumer, executor), new DefaultUbikServerSocketFactory());
    }
  }

  // --------------------------------------------------------------------------

  class TCPUnicastThreadPool extends WorkerPool<Request> {

    TCPUnicastThreadPool(EventConsumer consumer, ExecutorService executor) {
      super(executor);
    }

    @Override
    protected Worker<Request> newWorker() {
      return new TCPUnicastWorker();
    }
  }

  // --------------------------------------------------------------------------

  class TCPUnicastWorker implements Worker<Request> {

    private Category log = Log.createCategory(getClass());

    @Override
    public void execute(Request req) {

      try {
        Object o = req.getConnection().receive();

        if (o instanceof RemoteEvent) {
          RemoteEvent evt = (RemoteEvent) o;

          if (evt.isSync()) {
            if (context().getConsumer().hasSyncListener(evt.getType())) {
              log.debug("Received sync remote event %s from %s, notifying listener", evt.getType(), evt.getNode());
              Object response = context().getConsumer().onSyncEvent(evt);
              req.getConnection().send(new Response(req.getServerAddress(), evt.getId(), response));
            } else {
              log.debug("Received sync remote event %s from %s, no listener to notify", evt.getType(), evt.getNode());
              req.getConnection().send(new Response(req.getServerAddress(), evt.getId(), null).setNone());
            }
          } else {
            log.debug("Received async remote event %s from %s, notifying listeners", evt.getType(), evt.getNode());
            context().getConsumer().onAsyncEvent(evt);
          }
        } else {
          log.error("Object not a remote event: " + o.getClass().getName() + "; " + o);
        }
      } catch (Exception e) {
        log.error("Error caught handling request", e);
      }
    }
  }

}
