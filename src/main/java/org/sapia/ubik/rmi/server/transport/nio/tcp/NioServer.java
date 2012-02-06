package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.rmi.server.Server;

/**
 * This class implements the {@link Server} interface on top of a {@link SocketAcceptor}.
 * 
 * @author Yanick Duchesne
 * 
 */
class NioServer implements Server{

  private SocketAcceptor    acceptor;
  private InetSocketAddress inetAddr;
  private NioAddress        addr;
  private NioHandler        handler;
  private ExecutorService   executor;

  /**
   * This constructor is called by a {@link NioTcpTransportProvider} instance. The <code>maxThreads</code> 
   * argument allows specifying the maximum number of IO processor threads that will be used by this instance.
   * <p>
   * See the <a href="http://mina.apache.org/configuring-thread-model.html">threading model</a> page on Mina's site
   * for more details (the threading model for this class is based on Mina's {@link ExecutorFilter}).
   * 
   * @param inetAddr the {@link InetSocketAddress} on which the server should listen.
   * @param bufsize the size of buffers created internally to process data. 
   * @param maxThreads the maximum number of processor threads - if maxThreads <= 0, threads
   * will be created as needed (no maximum constraint).
   * 
   * @throws IOException if a problem occurs while creating this instance.
   */
  NioServer(InetSocketAddress inetAddr, int bufsize, int maxThreads) throws IOException {
    this.acceptor = new SocketAcceptor();
    if(maxThreads <= 0){
      Log.debug(getClass(), "Using a cached thread pool (no max threads)");
      this.executor = Executors.newCachedThreadPool();
    }
    else{
      Log.debug(getClass(), "Using maximum number of threads: " + maxThreads);      
      this.executor = Executors.newFixedThreadPool(maxThreads);
    }
    acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new NioCodecFactory()));
    acceptor.getFilterChain().addLast("threads", new ExecutorFilter(executor));    
    
    if(inetAddr.getPort() != 0){
      this.inetAddr = inetAddr;  
    }
    else{
      int randomPort = new TcpPortSelector().select(inetAddr.getAddress().getHostAddress());
      inetAddr = new InetSocketAddress(inetAddr.getAddress().getHostAddress(), randomPort);
    }
    addr = new NioAddress(inetAddr.getAddress().getHostAddress(), inetAddr.getPort());
    handler = new NioHandler(addr);    
  }
  
  /**
   * @see org.sapia.ubik.rmi.server.transport.nio.tcp.AddressProvider#getAddress()
   */
  public ServerAddress getAddress() {
    return getServerAddress();
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#getServerAddress()
   */
  public ServerAddress getServerAddress() {
    return addr;
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#start()
   */
  public void start() throws RemoteException {
    try {
      acceptor.bind(inetAddr, handler);
    } catch(IOException e) {
      throw new RemoteException("Could not start acceptor", e);
    }
  }

  /**
   * @see org.sapia.ubik.rmi.server.Server#close()
   */
  public void close() {
    acceptor.unbind(inetAddr);    
    executor.shutdown();
  }
}
