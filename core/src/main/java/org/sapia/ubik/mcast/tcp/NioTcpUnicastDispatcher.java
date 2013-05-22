package org.sapia.ubik.mcast.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
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
  
  public NioTcpUnicastDispatcher(EventConsumer consumer, int maxThreads, int marshallingBufferSize) throws IOException {
    super(consumer);
    this.handler    = new NioTcpUnicastHandler(consumer);
    this.maxThreads = maxThreads;
    this.marshallingBufferSize = marshallingBufferSize;
    String host = Localhost.getAnyLocalAddress().getHostAddress();
    socketAddress = new InetSocketAddress(host, new TcpPortSelector().select(host));
  }
  
  // --------------------------------------------------------------------------
  // UnicastDispatcher interface    
  
  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    return address;
  }

  // --------------------------------------------------------------------------
  // Inherited abstract methods
  
  @Override
  protected String doGetTransportType() {
    return TRANSPORT_TYPE;
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
    
    acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new NioTcpUnicastCodecFactory()));
    acceptor.getFilterChain().addLast("threads", new ExecutorFilter(executor));    
    
    log.info("Binding to address: %s", socketAddress);      
    this.address = new NioTcpUnicastAddress(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    try {
      acceptor.bind(socketAddress, handler);
    } catch (IOException e) {
      throw new IllegalStateException("Could not bind server to address " + socketAddress, e);
    }
  }

}
