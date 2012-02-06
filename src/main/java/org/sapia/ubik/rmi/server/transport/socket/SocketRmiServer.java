package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.DefaultUbikServerSocketFactory;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.SocketConnectionFactory;
import org.sapia.ubik.net.SocketServer;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.ThreadPool;
import org.sapia.ubik.net.UbikServerSocketFactory;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.stats.Statistic;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.util.Localhost;

/**
 * A standard socket server implementation - listening on given port (or on a randomly chosen port) 
 * for incoming {@link RMICommand} instances.
 *
 * @author Yanick Duchesne
 */
public class SocketRmiServer extends SocketServer implements Server, SocketRmiServerMBean {
    
  /**
   * A convenient {@link SocketRmiServer} builder.
   */
  public static class Builder {
    
    private String                  bindAddress;
    private int                     port;
    private long                    resetInterval;
    private SocketConnectionFactory connectionFactory;
    private ThreadPool<Request>     threadPool;
    private int                     maxThreads            = ThreadPool.NO_MAX;
    private UbikServerSocketFactory serverSocketFactory;
 
    /**
     * @param bindAddress the address to which the server should be bound (if not specified, the {@link Localhost} class 
     * is used to select one).
     * @return this instance.
     */
    public Builder setBindAddress(String bindAddress) {
      this.bindAddress = bindAddress;
      return this;
    }
    
    /**
     * @param port the port on which the server should listen - if not specified, a random port is selected.
     * @return this instance.
     */
    public Builder setPort(int port) {
      this.port = port;
      return this;
    }
    
    /**
     * @param maxThreads the maximum number of threads that the {@link ThreadPool} created by this instance
     * will handle.
     * @return this instance.
     */
    public Builder setMaxThreads(int maxThreads) {
      this.maxThreads = maxThreads;
      return this;
    }
    
    /**
     * If a  {@link #connectionFactory} is explicitely specified, this property will have no effect.
     * 
     * @param resetInterval the interval (in millis) at which the MarshalOutputStream will reset it's internal object cache.
     * @see #setConnectionFactory(SocketConnectionFactory)
     * @return this instance.
     */
    public Builder setResetInterval(long resetInterval) {
      this.resetInterval = resetInterval;
      return this;
    }
    
    /**
     * If the thread pool is specified, any value set by {@link #setMaxThreads(int)} will be ignored.
     * 
     * @param threadPool the {@link ThreadPool} that the server will be using.
     * @return this instance.
     */
    public Builder setThreadPool(ThreadPool<Request> threadPool) {
      this.threadPool = threadPool;
      return this;
    }

    /**
     * @param serverSocketFactory the {@link UbikServerSocketFactory} that the server will be using
     * to create a {@link ServerSocket} instance.
     * @return this instance.
     */
    public Builder setServerSocketFactory(UbikServerSocketFactory serverSocketFactory) {
      this.serverSocketFactory = serverSocketFactory;
      return this;
    }
    
    /**
     * @param connectionFactory the {@link SocketConnectionFactory} to use on the server-side, to handle 
     * communication with clients.
     * @return this instance.
     */
    public Builder setConnectionFactory(SocketConnectionFactory connectionFactory) {
      this.connectionFactory = connectionFactory;
      return this;
    }
    
    protected Builder() {}
   
    public static Builder create() {
      return new Builder();
    }
    
    // ------------------------------------------------------------------------
    
    public SocketRmiServer build() throws IOException {
      
      if(maxThreads > 0) {
        if(threadPool != null) {
          throw new IllegalStateException("Thread pool should not be assigned if max threads is specified");
        } 
        this.threadPool = new SocketRmiServerThreadPool("ubik.rmi.tcp.SocketServerThread", true, maxThreads);
      } else if (threadPool == null) {
        this.threadPool = new SocketRmiServerThreadPool("ubik.rmi.tcp.SocketServerThread", true, ThreadPool.NO_MAX);
      }
      
      if(connectionFactory == null) {
        SocketRmiConnectionFactory rmiConnectionFactory = new SocketRmiConnectionFactory();
        rmiConnectionFactory.setResetInterval(resetInterval);
        connectionFactory = rmiConnectionFactory;
      }
      
      if(serverSocketFactory == null) {
        serverSocketFactory = new DefaultUbikServerSocketFactory();
      }
      
      if(bindAddress != null) {
        return new SocketRmiServer(bindAddress, port, threadPool, connectionFactory, serverSocketFactory);
      } else {
        return new SocketRmiServer(port, threadPool, connectionFactory, serverSocketFactory);
      }
      
    }
    
  }

  // --------------------------------------------------------------------------
  
  private Statistic     threadCountStatistic;
  private ServerAddress addr;
  private Thread        serverThread;
  
  protected SocketRmiServer(
      String bindAddr, 
      int port, 
      ThreadPool<Request> tp,
      SocketConnectionFactory connectionFactory,
      UbikServerSocketFactory serverSocketFactory) throws IOException {
    super(bindAddr, 
          port, 
          connectionFactory,
          tp, 
          serverSocketFactory);
    addr = new TCPAddress(getAddress(), getPort());            
  }
    
    protected SocketRmiServer(
        int port, 
        ThreadPool<Request> tp,
        SocketConnectionFactory connectionFactory,
        UbikServerSocketFactory serverSocketFactory) throws IOException {
      super(port, 
            connectionFactory,
            tp, 
            serverSocketFactory);
    addr = new TCPAddress(getAddress(), getPort());            
  }
  
  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()()
   */
  public ServerAddress getServerAddress() {
    return addr;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() throws RemoteException {
    Log.debug(this.getClass(), "Starting server");

    serverThread = NamedThreadFactory.createWith("rmi.tcp.SocketServer").setDaemon(true).newThread(this);
    serverThread.start();

    try {
      waitStarted();
    } catch (InterruptedException e) {
      RemoteException re = new RemoteException("Thread interrupted during server startup", e);
      throw re;
    } catch (Exception e) {
      RemoteException re = new RemoteException("Error while starting up", e);
      throw re;
    }
    
    threadCountStatistic = new Statistic(getClass().getSimpleName(), "ThreadCount", "Number of currently active threads") {
      public double getStat() {
        return SocketRmiServer.this.getThreadCount();
      }
    };
    
    Stats.getInstance().add(threadCountStatistic);
  }
  
  @Override
  public void close() {
    try {
      super.close();
    } finally {
      ThreadShutdown.create(serverThread).shutdownLenient();
    }
  }
  
}
