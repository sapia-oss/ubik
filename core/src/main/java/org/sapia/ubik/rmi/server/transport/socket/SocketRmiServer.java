package org.sapia.ubik.rmi.server.transport.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.DefaultUbikServerSocketFactory;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.SocketConnectionFactory;
import org.sapia.ubik.net.SocketServer;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.UbikServerSocketFactory;
import org.sapia.ubik.net.WorkerPool;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Localhost;

/**
 * A standard socket server implementation - listening on given port (or on a
 * randomly chosen port) for incoming {@link RMICommand} instances.
 * 
 * @author Yanick Duchesne
 */
public class SocketRmiServer extends SocketServer implements Server, SocketRmiServerMBean {

  /**
   * A convenient {@link SocketRmiServer} builder.
   */
  public static class Builder {

    private String                 transportType;
    private String                 bindAddress;
    private int                    port;
    private long                   resetInterval;
    private SocketConnectionFactory connectionFactory;;
    private UbikServerSocketFactory serverSocketFactory;
    private ExecutorService         executor;

    private Builder(String transportType) {
      this.transportType = transportType;
    }
    
    /**
     * @param executor the {@link ExecutorService} to use for worker threads.
     * @return this instance.
     */
    public Builder setExecutor(ExecutorService executor) {
      this.executor = executor;
      return this;
    }

    /**
     * @param bindAddress
     *          the address to which the server should be bound (if not
     *          specified, the {@link Localhost} class is used to select one).
     * @return this instance.
     */
    public Builder setBindAddress(String bindAddress) {
      this.bindAddress = bindAddress;
      return this;
    }

    /**
     * @param port
     *          the port on which the server should listen - if not specified, a
     *          random port is selected.
     * @return this instance.
     */
    public Builder setPort(int port) {
      this.port = port;
      return this;
    }
    
    /**
     * If a {@link #connectionFactory} is explicitely specified, this property
     * will have no effect.
     * 
     * @param resetInterval
     *          the interval (in millis) at which the MarshalOutputStream will
     *          reset it's internal object cache.
     * @see #setConnectionFactory(SocketConnectionFactory)
     * @return this instance.
     */
    public Builder setResetInterval(long resetInterval) {
      this.resetInterval = resetInterval;
      return this;
    }

    /**
     * @param serverSocketFactory
     *          the {@link UbikServerSocketFactory} that the server will be
     *          using to create a {@link ServerSocket} instance.
     * @return this instance.
     */
    public Builder setServerSocketFactory(UbikServerSocketFactory serverSocketFactory) {
      this.serverSocketFactory = serverSocketFactory;
      return this;
    }

    /**
     * @param connectionFactory
     *          the {@link SocketConnectionFactory} to use on the server-side,
     *          to handle communication with clients.
     * @return this instance.
     */
    public Builder setConnectionFactory(SocketConnectionFactory connectionFactory) {
      this.connectionFactory = connectionFactory;
      return this;
    }

    protected Builder() {
    }

    public static Builder create(String transportType) {
      return new Builder(transportType);
    }

    // ------------------------------------------------------------------------

    public SocketRmiServer build() throws IOException {

      SocketRmiServerThreadPool threadPool = new SocketRmiServerThreadPool(Threads.createWorkerPool());

      if (connectionFactory == null) {
        SocketRmiConnectionFactory rmiConnectionFactory = new SocketRmiConnectionFactory(transportType);
        rmiConnectionFactory.setResetInterval(resetInterval);
        connectionFactory = rmiConnectionFactory;
      }

      if (serverSocketFactory == null) {
        serverSocketFactory = new DefaultUbikServerSocketFactory();
      }

      if (bindAddress != null) {
        return new SocketRmiServer(transportType, bindAddress, port, threadPool, connectionFactory, serverSocketFactory);
      } else {
        return new SocketRmiServer(transportType, port, threadPool, connectionFactory, serverSocketFactory);
      }

    }

  }

  // --------------------------------------------------------------------------

  private ServerAddress addr;
  private Thread serverThread;

  protected SocketRmiServer(String transportType, String bindAddr, int port, WorkerPool<Request> tp, SocketConnectionFactory connectionFactory,
      UbikServerSocketFactory serverSocketFactory) throws IOException {
    super(bindAddr, port, connectionFactory, tp, serverSocketFactory);
    addr = new TCPAddress(transportType, getAddress(), getPort());
  }

  protected SocketRmiServer(String transportType, int port, WorkerPool<Request> tp, SocketConnectionFactory connectionFactory,
      UbikServerSocketFactory serverSocketFactory) throws IOException {
    super(port, connectionFactory, tp, serverSocketFactory);
    addr = new TCPAddress(transportType, getAddress(), getPort());
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
