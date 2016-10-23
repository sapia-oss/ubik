package org.sapia.ubik.mcast.tcp;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.concurrent.BlockingCompletionQueue;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.RespList;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.TimeoutException;
import org.sapia.ubik.mcast.UnicastDispatcher;
import org.sapia.ubik.mcast.UnicastDispatcherSupport;
import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ConnectionPool;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.TimeValue;
import org.sapia.ubik.util.exception.RuntimeCheckedException;
import org.sapia.ubik.util.exception.RuntimeIoException;
import org.sapia.ubik.util.pool.PooledObjectCreationException;

/**
 * Base implementation for TCP-based {@link UnicastDispatcher}s.
 *
 * @author yduchesne
 *
 */
public abstract class BaseTcpUnicastDispatcher extends UnicastDispatcherSupport implements UnicastDispatcher {

  private Stopwatch syncSend      = Stats.createStopwatch(
      getClass(), "SyncSendTime", "Time required to send synchronously"
  );
  private Stopwatch asyncDispatch = Stats.createStopwatch(
      getClass(), "AsyncDispatchTime", "Time required to dispatch asynchronously"
  );

  protected Category log = Log.createCategory(getClass());
  protected EventConsumer   consumer;
  protected ConnectionPools connections           = new ConnectionPools();
  private TimeValue         asyncAckTimeout       = Defaults.DEFAULT_SYNC_RESPONSE_TIMEOUT;
  private int               senderCount           = Defaults.DEFAULT_SENDER_COUNT;
  private int               maxConnectionsPerHost = Defaults.DEFAULT_MAX_CONNECTIONS_PER_HOST;
  private ExecutorService   senders;

  protected BaseTcpUnicastDispatcher() {
  }

  @Override
  public void initialize(EventConsumer consumer, Conf config) {
    this.consumer = consumer;
    setSenderCount(config.getIntProperty(Consts.MCAST_SENDER_COUNT, Defaults.DEFAULT_SENDER_COUNT));
    setMaxConnectionsPerHost(config.getIntProperty(Consts.MCAST_MAX_CLIENT_CONNECTIONS, Defaults.DEFAULT_MAX_CONNECTIONS_PER_HOST));
    setAsyncAckTimeout(config.getTimeProperty(Consts.MCAST_ASYNC_ACK_TIMEOUT, Defaults.DEFAULT_ASYNC_ACK_TIMEOUT));
  }

  /**
   * Sets the number of threads used to send remote events.
   *
   * @param senderCount
   *          the number of sender threads.
   *
   * @see #send(List, String, Object)
   */
  void setSenderCount(int senderCount) {
    Assertions.isTrue(senderCount > 0, "Sender count must be greater than 0: %s", senderCount);
    this.senderCount = senderCount;
  }

  /**
   * @param maxConnectionsPerHost
   *          the maximum number of connections to pool, by host.
   */
  void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
    Assertions.isTrue(maxConnectionsPerHost > 0, "Max connections per host must be greater than 0: %s", maxConnectionsPerHost);
    this.maxConnectionsPerHost = maxConnectionsPerHost;
  }
  
  /**
   * @param asyncAckTimeout the timeout to observe when waiting for async event acknowledgement.
   */
  void setAsyncAckTimeout(TimeValue asyncAckTimeout) {
    Assertions.isTrue(asyncAckTimeout.getValue() >= 0, "Async ack must be equal to or greater than 0: %s", asyncAckTimeout);
    this.asyncAckTimeout = asyncAckTimeout;
  }

  @Override
  public void start() {
    Assertions.illegalState(consumer == null, "EventConsumer not set");
    log.debug("Starting...");
    this.senders = Executors.newFixedThreadPool(senderCount, NamedThreadFactory.createWith("tcp.unicast.dispatcher.Sender").setDaemon(true));
    doStart();
    log.debug("Started");
  }

  @Override
  public void close() {
    log.debug("Closing...");
    try {
      doClose();
    } finally {
      senders.shutdown();
    }
    connections.shutdown();
    log.debug("Closed");
  }

  @Override
  public RespList send(List<ServerAddress> addresses, final String type, Object data, final TimeValue timeout) throws IOException, InterruptedException {

    final BlockingCompletionQueue<Response> queue = new BlockingCompletionQueue<Response>(addresses.size());
    final RemoteEvent evt = new RemoteEvent(null, type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(getAddress());

    for (int i = 0; i < addresses.size(); i++) {
      final TCPAddress addr = (TCPAddress) addresses.get(i);

      senders.execute(new Runnable() {

        @Override
        public void run() {
          Split split = syncSend.start();
          try {
            queue.add((Response) doSendAsync(addr, evt, true, type, timeout));
          } catch (Exception e) {
            handleException(queue, e, evt, addr);
          } finally {
            split.stop();
          }
        }
      });
    }
    
    RespList responses = new RespList(queue.await(timeout.getValueInMillis()));
    log.debug("Returning %s responses", responses.count());
    return responses;
  }   
 
  @Override
  public RespList send(ServerAddress[] addresses, final String type, Object[] data, final TimeValue timeout) throws IOException, InterruptedException {

    final BlockingCompletionQueue<Response> queue = new BlockingCompletionQueue<Response>(addresses.length);


    for (int i = 0; i < addresses.length; i++) {
      final TCPAddress addr = (TCPAddress) addresses[i];
      final RemoteEvent evt = new RemoteEvent(null, type, data[i]).setNode(consumer.getNode()).setSync();
      evt.setUnicastAddress(getAddress());
      
      senders.execute(new Runnable() {

        @Override
        public void run() {
          Split split = syncSend.start();
          try {
            queue.add((Response) doSendAsync(addr, evt, true, type, timeout));
          } catch (Exception e) {
            handleException(queue, e, evt, addr);
          } finally {
            split.stop();
          }
        }
      });
    }

    RespList responses = new RespList(queue.await(timeout.getValueInMillis()));
    log.debug("Returning %s responses", responses.count());
    return responses;
  }

  @Override
  public Response send(ServerAddress addr, String type, Object data, final TimeValue timeout) throws IOException {

    RemoteEvent evt = new RemoteEvent(null, type, data).setNode(consumer.getNode()).setSync();
    evt.setUnicastAddress(addr);

    Split split = syncSend.start();

    try {
      return (Response) doSendAsync(addr, evt, true, type, timeout);
    } catch (ClassNotFoundException e) {
      log.warning("Could not deserialize response from %s", e, addr);
      return new Response(addr, evt.getId(), e);
    } catch (TimeoutException e) {
      log.warning("Response from %s not received in timely manner", addr);
      return new Response(addr, evt.getId(), e).setStatusSuspect();
    } catch (ConnectException e) {
      log.warning("Remote node probably down: %s", e, addr);
      return new Response(addr, evt.getId(), e).setStatusSuspect();
    } catch (RemoteException e) {
      log.warning("Remote node probably down: %s", e, addr);
      return new Response(addr, evt.getId(), e).setStatusSuspect();
    } catch (IOException e) {
      log.warning("IO error caught trying to send to %s", e, addr);
      return new Response(addr, evt.getId(), e);
    } catch (InterruptedException e) {
      ThreadInterruptedException tie = new ThreadInterruptedException();
      throw tie;
    } finally {
      split.stop();
    }
  }

  @Override
  public boolean dispatch(final ServerAddress addr, final String type, final Object data) throws IOException {
    Split split = asyncDispatch.start();
    try {
      RemoteEvent evt = new RemoteEvent(null, type, data).setNode(consumer.getNode());
      evt.setUnicastAddress(getAddress());
      log.debug("dispatch() to %s, type: %s, data: %s", addr, type, data);
      
      doSendAsync(addr, evt, false, type, asyncAckTimeout);
      return true;
    } catch (RemoteException e) {
      log.warning("Could not send: node probably down %s", e, addr);
      return false;
    } catch (ClassNotFoundException e) {
      log.warning("Could not deserialize response", e);
      return false;
    } catch (TimeoutException e) {
      log.warning("Did not receive ack from peer", e);
      return false;
    } catch (InterruptedException e) {
      throw new ThreadInterruptedException();
    } catch (IOException e) {
      throw new RuntimeIoException(e);
    } finally {
      split.stop();
    }

  }
  
  private Object doSendAsync(
      final ServerAddress addr, 
      final Serializable toSend, 
      final boolean synchro, 
      final String type, 
      final TimeValue timeout) 
      throws 
        IOException, ClassNotFoundException, TimeoutException, 
        InterruptedException, RemoteException {
    
    Callable<Object> task = new Callable<Object>() {
      public Object call() {
        try {
          return doSendSync(addr, toSend, synchro, type, timeout);
        } catch (RuntimeException e) {
          throw e;
        } catch (Exception e) {
          throw new RuntimeCheckedException(e);
        }
      }
    };
    
    Future<Object> result = senders.submit(task);
    try {
      return result.get(asyncAckTimeout.getValue(), asyncAckTimeout.getUnit());
    } catch (ExecutionException exe) {
      if (exe.getCause() instanceof RuntimeCheckedException) {
        RuntimeCheckedException rte = (RuntimeCheckedException) exe.getCause();
        if (rte.getCause() instanceof IOException) {
          throw (IOException) rte.getCause();
        } else if (rte.getCause() instanceof TimeoutException) {
          throw (TimeoutException) rte.getCause();
        } else if (rte.getCause() instanceof ClassNotFoundException) {
          throw (ClassNotFoundException) rte.getCause();
        } else {
          throw new IllegalStateException("Unexpected error occurred", rte.getCause());
        }
      } else if (exe.getCause() instanceof RuntimeException) {
        throw (RuntimeException) exe.getCause();
      } else {
        throw new IllegalStateException("Unexpected error occurred", exe.getCause());
      }
    } catch (java.util.concurrent.TimeoutException e) {
      throw new TimeoutException();
    } catch (InterruptedException e) {
      throw e;
    }
  }
  
  private Object doSendSync(ServerAddress addr, Serializable toSend, boolean synchro, String type, TimeValue timeout) throws IOException, ClassNotFoundException,
      TimeoutException, InterruptedException, RemoteException {
    log.debug("doSend() : %s, event type: %s", addr, type);
    ConnectionPool pool = connections.getPoolFor(addr);
    Connection connection = null;
    try {
      connection = pool.acquire();
    } catch (PooledObjectCreationException e) {
      if (e.getCause() instanceof ConnectException || e.getCause() instanceof RemoteException) {
        pool.clear();
        try {
          connection = pool.acquire();
        } catch (PooledObjectCreationException e2) {
          if (e2.getCause() instanceof ConnectException) {
            throw new RemoteException("Could not connect to " + addr, e.getCause());
          } else if (e2.getCause() instanceof RemoteException) {
            throw (RemoteException) e.getCause();
          } else {
            throw new RemoteException("Undetermined error caught connecting to " + addr, e.getCause());
          }
        }
      }
    }

    try {
      connection.send(toSend);
    } catch (RemoteException re) {
      pool.invalidate(connection);
      pool.clear();

      try {
        connection = pool.acquire();
        connection.send(toSend);
      } catch (RemoteException re2) {
        pool.invalidate(connection);
        throw re;
      } catch (PooledObjectCreationException e) {
        pool.invalidate(connection);
        throw re;
      }
    }

    if (synchro) {
      try {
        Object toReturn = connection.receive(timeout.getValueInMillis());
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

  // --------------------------------------------------------------------------
  // Abstract methods

  protected abstract void doStart();

  protected abstract void doClose();

  protected abstract String doGetTransportType();

  protected abstract ConnectionFactory doGetConnectionFactory();

  // ==========================================================================
  // Inner classes

  class ConnectionPools {

    private Map<ServerAddress, ConnectionPool> pools = new ConcurrentHashMap<ServerAddress, ConnectionPool>();

    synchronized ConnectionPool getPoolFor(ServerAddress addr) {
      ConnectionPool pool = pools.get(addr);
      if (pool == null) {
        TCPAddress tcpAddr = (TCPAddress) addr;
        ConnectionFactory sockets = doGetConnectionFactory();
        pool = new ConnectionPool.Builder().host(tcpAddr.getHost()).port(tcpAddr.getPort()).maxSize(maxConnectionsPerHost).connectionFactory(sockets)
            .build();
        pools.put(addr, pool);
      }
      return pool;
    }

    void shutdown() {
      for (ConnectionPool pool : pools.values()) {
        pool.shrinkTo(0);
      }
    }
  }
}
