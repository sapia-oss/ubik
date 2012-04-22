package org.sapia.ubik.mcast.tcp;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.ubik.concurrent.BlockingCompletionQueue;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.RespList;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.TimeoutException;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ConnectionPool;
import org.sapia.ubik.net.DefaultUbikServerSocketFactory;
import org.sapia.ubik.net.PooledThread;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.SocketConnectionFactory;
import org.sapia.ubik.net.SocketServer;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.net.ThreadPool;
import org.sapia.ubik.util.Localhost;

/**
 * Implements a {@link UnicastDispatcher} over TCP.
 * 
 * @author yduchesne
 *
 */
public class TCPUnicastDispatcher implements UnicastDispatcher {
  
  public static final String MAX_CONNECTIONS = "ubik.rmi.naming.unicast.tcp.max-connections";
  public static final String TRANSPORT_TYPE  = "tcp/unicast";

  public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST   = 3;
  
  private Category                log                   = Log.createCategory(getClass());
  private EventConsumer           consumer;
  private ConnectionPools         connections           = new ConnectionPools();
  private int                     responseTimeout       = Defaults.DEFAULT_SYNC_RESPONSE_TIMEOUT;
  private int                     senderCount           = Defaults.DEFAULT_SENDER_COUNT;
  private int                     maxConnectionsPerHost = DEFAULT_MAX_CONNECTIONS_PER_HOST;
  private TCPUnicastSocketServer  socketServer;
  private ServerAddress           address;
  private Thread                  serverThread;
  private ExecutorService         senders;
  
  /**
   * @param consumer
   * @throws IOException
   */
  public TCPUnicastDispatcher(EventConsumer consumer, int maxThreads) throws IOException{
    if(Localhost.isIpPatternDefined()) {
      this.socketServer = new TCPUnicastSocketServer(Localhost.getAnyLocalAddress().getHostAddress(), consumer, maxThreads);  
    } else {
      this.socketServer = new TCPUnicastSocketServer(consumer, maxThreads);
    }
    this.consumer = consumer;
  }
  
  /**
   * Sets this instance's response timeout.
   * @param responseTimeout the number of millis to wait for synchronous responses.
   */
  public void setResponseTimeout(int responseTimeout) {
    this.responseTimeout = responseTimeout;
  }
  
  /**
   * Sets the number of threads used to send remote events.
   * 
   * @param senderCount the number of sender threads.
   * 
   * @see #send(List, String, Object)
   */
  public void setSenderCount(int senderCount) {
    this.senderCount = senderCount;
  }
  
  /**
   * @param maxConnectionsPerHost the maximum number of connections to pool, by host.
   */
  public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
    this.maxConnectionsPerHost = maxConnectionsPerHost;
  }
  
  @Override
  public void start() {
    log.debug("Starting...");
    if(serverThread != null && serverThread.isAlive()) {
      throw new IllegalStateException("Server already started");
    }
    
    this.senders = Executors.newFixedThreadPool(
        senderCount, 
        NamedThreadFactory.createWith("tcp.unicast.dispatcher.Sender").setDaemon(true)
    );

    serverThread = NamedThreadFactory.createWith("tcp.unicast.dispatcher.Server")
      .setDaemon(true)
      .newThread(socketServer);
    serverThread.start();
    try{
      socketServer.waitStarted();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Thread interrupted while waiting for socket server startup", e);
    } catch (Exception e) {
      throw new IllegalStateException("Could not start socket server", e);
    }
    log.debug("Started");
  }
  
  @Override
  public void close() {
    log.debug("Closing...");
    socketServer.close();
    ThreadShutdown.create(serverThread).shutdownLenient();
    log.debug("Closed");
  }
  
  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    if (address == null) {
      address = new TCPAddress(TRANSPORT_TYPE, socketServer.getAddress(), socketServer.getPort());
      log.debug("Server address for node %s: %s", consumer.getNode(), address);      
    }
    return address;
  }
  
  @Override
  public RespList send(List<ServerAddress> addresses, final String type, Object data)
      throws IOException, InterruptedException {
    
    final BlockingCompletionQueue<Response> queue = new BlockingCompletionQueue<Response>(addresses.size());
    
    final RemoteEvent evt = new RemoteEvent(null, type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(getAddress());

    for (int i = 0; i < addresses.size(); i++) {
      final TCPAddress addr = (TCPAddress) addresses.get(i);
      
      senders.execute(new Runnable() {
        
        public void run() {
          try {
            queue.add((Response) doSend(addr, evt, true, type));
          } catch (ClassNotFoundException e) {
            log.error("Could not deserialize response received from " + addr, e);
            try {
            	queue.add(new Response(evt.getId(), e));
            } catch (IllegalStateException ise) {
            	log.info("Could not add response to queue", ise, new Object[]{});
            }
          } catch (TimeoutException e) {
            log.error("Response from %s not received in timely manner", addr);
            try {
            	queue.add(new Response(evt.getId(), e).setStatusSuspect());
            } catch (IllegalStateException ise) {
            	log.info("Could not add response to queue", ise, new Object[]{});
            }            	
          } catch (ConnectException e) {
            log.error("Remote node probably down: %s" + addr, e);
            try {
            	queue.add(new Response(evt.getId(), e).setStatusSuspect());
            } catch (IllegalStateException ise) {
            	log.info("Could not add response to queue", ise, new Object[]{});
            }            	
          } catch (RemoteException e) {
            log.error("Remote node probably down: %s" + addr, e);
            try {
            	queue.add(new Response(evt.getId(), e).setStatusSuspect());
            } catch (IllegalStateException ise) {
            	log.info("Could not add response to queue", ise, new Object[]{});
            }             
          } catch (IOException e) {
            log.error("IO error caught trying to send to %s" + addr, e);
            try {
            	queue.add(new Response(evt.getId(), e));
            } catch (IllegalStateException ise) {
            	log.info("Could not add response to queue", ise, new Object[]{});
            }
          } catch (InterruptedException e) {
            ThreadInterruptedException tie = new ThreadInterruptedException();
            tie.fillInStackTrace();
            throw tie;
          }
        }
      });
    }
    
    return new RespList(queue.await(responseTimeout));
  }
  
  @Override
  public Response send(ServerAddress addr, String type, Object data)
      throws IOException {

    RemoteEvent evt = new RemoteEvent(null, type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(addr);
    try {
      return (Response) doSend(addr, evt, true, type);
    } catch (ClassNotFoundException e) {
      log.error("Could not deserialize response from %s" + addr, e);
      return new Response(evt.getId(), e);      
    } catch (TimeoutException e) {
      log.error("Response from %s not received in timely manner", addr);
      return new Response(evt.getId(), e).setStatusSuspect();
    } catch (ConnectException e) {
      log.error("Remote node probably down: %s" + addr, e);
      return new Response(evt.getId(), e).setStatusSuspect();
    } catch (RemoteException e) {
      log.error("Remote node probably down: %s" + addr, e);
      return new Response(evt.getId(), e).setStatusSuspect();
    } catch (IOException e) {
      log.error("IO error caught trying to send to %s" + addr, e);
      return new Response(evt.getId(), e); 
    } catch (InterruptedException e) {
      ThreadInterruptedException tie = new ThreadInterruptedException();
      tie.fillInStackTrace();
      throw tie;
    }
  }
  
  @Override
  public void dispatch(ServerAddress addr, String type, Object data)
      throws IOException {
    
    try {
      RemoteEvent evt  = new RemoteEvent(null, type, data).setNode(consumer.getNode());
      evt.setUnicastAddress(getAddress());
      log.debug("dispatch() to %s, type: %s, data: %s", addr, type, data);
      doSend(addr, evt , false, type);
    } catch (ClassNotFoundException e) {
      log.error("Could not deserialize response", e);
    } catch (TimeoutException e) {
    	log.error("Did not receive ack from peer", e);
    } catch (InterruptedException e) {
      ThreadInterruptedException tie = new ThreadInterruptedException();
      tie.fillInStackTrace();
      throw tie;
    }
    
  }
  
  private Object doSend(ServerAddress addr, Serializable toSend, boolean synchro, String type) 
    throws IOException, ClassNotFoundException, TimeoutException, InterruptedException, RemoteException {
    log.debug("doSend() : %s, event type: %s", addr, type);
    ConnectionPool pool   = connections.getPoolFor(addr);
    Connection connection = pool.acquire();
    try {
      connection.send(toSend);
    } catch (RemoteException re) {
      pool.invalidate(connection);
      pool.clear();
      connection = pool.acquire();
      try {
        connection.send(toSend);
      } catch (RemoteException re2) {
        pool.invalidate(connection);
        throw re;
      }
    } 
    
    if (synchro) {
      try {
        Object toReturn = connection.receive();
        pool.release(connection);
        return toReturn;
      } catch (SocketTimeoutException e) {
        pool.invalidate(connection);
        TimeoutException toe = new TimeoutException();
        throw toe;
      } catch (IOException e) {
        pool.invalidate(connection);
        throw e;
      } catch (ClassNotFoundException e) {
        pool.invalidate(connection);
        throw e;
      }
    } else {
      pool.release(connection);
      return null;
    }
  }

  // ==========================================================================
  
  static class TCPUnicastSocketServer extends SocketServer {
    
    public TCPUnicastSocketServer(String bindAddress, EventConsumer consumer, int maxThreads) throws IOException{
      super(TRANSPORT_TYPE, bindAddress, 0, new TCPUnicastThreadPool(consumer, maxThreads), new DefaultUbikServerSocketFactory());
    }
    
    public TCPUnicastSocketServer(EventConsumer consumer, int maxThreads) throws IOException{
      super(TRANSPORT_TYPE, 0, new TCPUnicastThreadPool(consumer, maxThreads), new DefaultUbikServerSocketFactory());
    }    
  }

  // --------------------------------------------------------------------------
  
  static class TCPUnicastThreadPool extends ThreadPool<Request> {
    
    private EventConsumer consumer;
    
    TCPUnicastThreadPool(EventConsumer consumer, int maxSize) {
      super(consumer.getNode() + "Unicast@" + consumer.getDomainName().toString(), true, maxSize);
      this.consumer = consumer;
    }
   
    @Override
    protected PooledThread<Request> newThread(String name) throws Exception {
      return new TCPUnicastPooledThread("tcp.unicast.Dispatcher", consumer);
    }
  }
  
  // --------------------------------------------------------------------------
  
  static class TCPUnicastPooledThread extends PooledThread<Request> {
    
    private Category      log       = Log.createCategory(getClass());
    private EventConsumer consumer;
    
    public TCPUnicastPooledThread(String name, EventConsumer consumer) {
      super(name);
      this.consumer = consumer;
    }
    
    @Override
    protected void handleExecutionException(Exception e) {}
    
    @Override
    protected void doExec(Request req) {

      try {
        Object o = req.getConnection().receive();
        
        if (o instanceof RemoteEvent) {
          RemoteEvent evt = (RemoteEvent) o;
          
          if (evt.isSync()) {
            if (consumer.hasSyncListener(evt.getType())) {
              log.debug("Received sync remote event %s from %s, notifying listener", evt.getType(), evt.getNode());
              Object response = consumer.onSyncEvent(evt);
              req.getConnection().send(new Response(evt.getId(), response));
            } else {
              log.debug("Received sync remote event %s from %s, no listener to notify", evt.getType(), evt.getNode());
              req.getConnection().send(new Response(evt.getId(), null).setNone());
            }
          } else {
            log.debug("Received async remote event %s from %s, no listener to notify", evt.getType(), evt.getNode());
            consumer.onAsyncEvent(evt);
          }
        } else {
          log.error("Object not a remote event: " + o.getClass().getName() + "; " + o);
        }
      } catch (Exception e) {
        log.error("Error caught handling request", e);
      } 
    }
  }
  
  // ==========================================================================
  
  class ConnectionPools  {
 
    private Map<ServerAddress, ConnectionPool> pools = new ConcurrentHashMap<ServerAddress, ConnectionPool>();
    
    synchronized ConnectionPool getPoolFor(ServerAddress addr) {
      ConnectionPool pool = pools.get(addr);
      if(pool == null) {
        TCPAddress tcpAddr = (TCPAddress) addr;
        SocketConnectionFactory sockets = new SocketConnectionFactory(TRANSPORT_TYPE);
        sockets.setSoTimeout(responseTimeout);
        pool = new ConnectionPool.Builder()
                 .host(tcpAddr.getHost())
                 .port(tcpAddr.getPort())
                 .maxSize(maxConnectionsPerHost)
                 .connectionFactory(sockets)
                 .build();
        pools.put(addr, pool);
      }
      return pool;
    }
    
    void shutdown() {
      for(ConnectionPool pool : pools.values()) {
        pool.shrinkTo(0);
      }
    }
    
  }
}
