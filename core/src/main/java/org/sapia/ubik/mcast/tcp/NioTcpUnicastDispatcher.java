package org.sapia.ubik.mcast.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Localhost;

/**
 * A TCP unicast dispatcher based on NIO.
 * 
 * @author yduchesne
 *
 */
public class NioTcpUnicastDispatcher extends BaseTcpUnicastDispatcher {
  
  public static final class NioTcpUnicastAddress extends TCPAddress {

    /** Meant for serialization only */
    public NioTcpUnicastAddress() {
    }
    
    public NioTcpUnicastAddress(String host, int port) {
      super(TRANSPORT_TYPE, host, port);
    }
  }
  
  // ==========================================================================

  public static final String TRANSPORT_TYPE  = "tcp-nio/unicast";

  private Category          log         = Log.createCategory(getClass());

  private NioTcpUnicastHandler handler;
  private SocketAcceptor    acceptor;
  private ExecutorService   executor; 
  private ServerAddress     address;
  private InetSocketAddress socketAddress;
  private int               maxThreads;
  private int               marshallingBufferSize;
  
  public NioTcpUnicastDispatcher(EventConsumer consumer, int maxThreads, int marshallingBufferSize) {
    super(consumer);
    this.handler    = new NioTcpUnicastHandler(consumer);
    this.maxThreads = maxThreads;
    this.marshallingBufferSize = marshallingBufferSize;
  }
  
  // --------------------------------------------------------------------------
  // UnicastDispatcher interface    
  
  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    if (address == null) {
      throw new IllegalStateException("ServerAddress not set");
    }
    return address;
  }

  // --------------------------------------------------------------------------
  // Inherited abstract methods
  
  @Override
  protected String doGetTransportType() {
    return TRANSPORT_TYPE;
  }
  
  @Override
  protected void doStart() {
    acceptor = new SocketAcceptor(Runtime.getRuntime().availableProcessors() + 1, Executors.newCachedThreadPool());
    if(maxThreads <= 0){
      log.info("Using a cached thread pool (no max threads)");
      this.executor = Executors.newCachedThreadPool();
    } 
    else{
      log.info("Using maximum number of threads: %s", maxThreads);      
      this.executor = Executors.newFixedThreadPool(maxThreads);
    }   
    
    int port = 0;
    try {
      socketAddress = new InetSocketAddress(Localhost.getAnyLocalAddress().getHostAddress(), port);
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Could not create bind address", e);
    }
    
    acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new NioTcpUnicastCodecFactory()));
    acceptor.getFilterChain().addLast("threads", new ExecutorFilter(executor));    
    
    if(socketAddress.getPort() != 0){
      log.info("Using port %s", socketAddress.getPort());
    }
    else{
      try {
        int randomPort = new TcpPortSelector().select(socketAddress.getAddress().getHostAddress());
        log.info("Using random port %s", randomPort);      
        socketAddress = new InetSocketAddress(socketAddress.getAddress().getHostAddress(), randomPort);
      } catch (IOException e) {
        throw new IllegalStateException("Could not determine random port", e);
      }
    }
    
    log.info("Binding to address: %s", socketAddress);      
    this.address = new NioTcpUnicastAddress(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    try {
      acceptor.bind(socketAddress, handler);
    } catch (IOException e) {
      throw new IllegalStateException("Could not bind server to address " + socketAddress, e);
    }
  }
  
  @Override
  protected void doClose() {
    acceptor.unbind(socketAddress);    
    executor.shutdown();
  }
  
  @Override
  protected ConnectionFactory doGetConnectionFactory(int soTimeout) {
    return new NioTcpUnicastConnectionFactory(marshallingBufferSize);
  }

  
  public static void main(String[] args) throws Exception {
    Log.setDebug();
    System.setProperty(Consts.IP_PATTERN_KEY, "192\\.168\\.\\d+\\.\\d+");
    NioTcpUnicastDispatcher dispatcher = new NioTcpUnicastDispatcher(new EventConsumer("test"), 3, 512);
    dispatcher.start();
    Thread.sleep(5000);
    dispatcher.close();
  }
}
