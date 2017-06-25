package org.sapia.ubik.mcast.tcp.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioProcessor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.DispatcherContext;
import org.sapia.ubik.mcast.tcp.BaseTcpUnicastDispatcher;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.Localhost;

/**
 * A TCP unicast dispatcher based on Mina.
 *
 * @author yduchesne
 *
 */
public class MinaTcpUnicastDispatcher extends BaseTcpUnicastDispatcher {

  private Category log = Log.createCategory(getClass());

  private MinaTcpUnicastHandler handler;
  private SocketAcceptor        acceptor;
  private ServerAddress         address;
  private InetSocketAddress     socketAddress;
  private ExecutorService       selectorThreads;
  private ExecutorService       workerThreads;
  private int                   bufferSize;

  public MinaTcpUnicastDispatcher( ){
  }
  
  // --------------------------------------------------------------------------
  // UnicastDispatcher interface
  
  @Override
  public void initialize(DispatcherContext context) {  
    super.initialize(context);
    
    selectorThreads = context.getSelectorThreads().getExecutor("unicast.mina");
    workerThreads   = context.getWorkerThreads();
    bufferSize      = context.getConf().getIntProperty(Consts.MARSHALLING_BUFSIZE, Defaults.DEFAULT_MARSHALLING_BUFSIZE);
   
    this.handler = new MinaTcpUnicastHandler(context.getConsumer(), new Func<ServerAddress, Void>() {
      @Override
      public ServerAddress call(Void arg) {
        Assertions.illegalState(address == null, "Server address not set");
        return address;
      }
    });

    try { 
      String host = Localhost.getPreferredLocalAddress().getHostAddress();
      log.debug("Will bind server to address: %s", host);
      socketAddress = new InetSocketAddress(host, new TcpPortSelector().select());
    } catch (IOException e) {
      throw new IllegalStateException("Could not configure", e);
    }
    
 
  }

  @Override
  public ServerAddress getAddress() throws IllegalStateException {
    return address;
  }

  // --------------------------------------------------------------------------
  // Inherited abstract methods

  @Override
  protected String doGetTransportType() {
    return MinaTcpUnicastAddress.TRANSPORT_TYPE;
  }

  @Override
  protected void doClose() {
    acceptor.unbind(socketAddress);
    acceptor.dispose();
    // precaution: acceptor.dispose() should have shut down this thread pool
    selectorThreads.shutdownNow();
    workerThreads.shutdown();
  }

  @Override
  protected ConnectionFactory doGetConnectionFactory() {
    return new MinaTcpUnicastConnectionFactory(bufferSize);
  }

  @Override
  protected void doStart() {
    acceptor = new NioSocketAcceptor(new NioProcessor(selectorThreads));
    acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new MinaTcpUnicastCodecFactory()));
    acceptor.getFilterChain().addLast("threads", new ExecutorFilter(workerThreads));
    acceptor.setHandler(handler);
    acceptor.getSessionConfig().setReadBufferSize(bufferSize);
    acceptor.setReuseAddress(true);
    
    log.info("Binding to address: %s", socketAddress);
    this.address = new MinaTcpUnicastAddress(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    try {
      acceptor.bind(socketAddress);
    } catch (IOException e) {
      throw new IllegalStateException("Could not bind server to address " + socketAddress, e);
    }
  }

}
