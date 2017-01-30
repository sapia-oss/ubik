package org.sapia.ubik.mcast.tcp.mina;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.Defaults;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.tcp.BaseTcpUnicastDispatcher;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Conf;
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
  private ExecutorService       executor;
  private ServerAddress         address;
  private InetSocketAddress     socketAddress;
  private int                   maxThreads;
  private int                   marshallingBufferSize;

  public MinaTcpUnicastDispatcher(){
  }

  @Override
  public void initialize(EventConsumer consumer, Conf config) {
    super.initialize(consumer, config);
    maxThreads = config.getIntProperty(Consts.MCAST_HANDLER_COUNT, Defaults.DEFAULT_HANDLER_COUNT);
    marshallingBufferSize = config.getIntProperty(Consts.MARSHALLING_BUFSIZE, Consts.DEFAULT_MARSHALLING_BUFSIZE);
    
    this.handler = new MinaTcpUnicastHandler(consumer, new Func<ServerAddress, Void>() {
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
    return MinaTcpUnicastAddress.TRANSPORT_TYPE;
  }

  @Override
  protected void doClose() {
    acceptor.unbind(socketAddress);
    executor.shutdown();
  }

  @Override
  protected ConnectionFactory doGetConnectionFactory() {
    return new MinaTcpUnicastConnectionFactory(marshallingBufferSize);
  }

  @Override
  protected void doStart() {
    acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
    if (maxThreads <= 0) {
      log.info("Using a cached thread pool (no max threads)");
      this.executor = Executors.newCachedThreadPool(NamedThreadFactory.createWith("Ubik.MinaTcpUnicastDispatcher.HandlerThread").setDaemon(true));
    } else {
      log.info("Using maximum number of threads: %s", maxThreads);
      this.executor = Executors.newFixedThreadPool(maxThreads, NamedThreadFactory.createWith("Ubik.MinaTcpUnicastDispatcher.HandlerThread").setDaemon(true));
    }

    acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new MinaTcpUnicastCodecFactory()));
    acceptor.getFilterChain().addLast("threads", new ExecutorFilter(executor));
    acceptor.setHandler(handler);
    acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, (int) Defaults.DEFAULT_MINA_IDLE_TIME.getValueInSeconds());
    acceptor.getSessionConfig().setReadBufferSize(marshallingBufferSize);
    log.info("Binding to address: %s", socketAddress);
    this.address = new MinaTcpUnicastAddress(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    try {
      acceptor.bind(socketAddress);
    } catch (IOException e) {
      throw new IllegalStateException("Could not bind server to address " + socketAddress, e);
    }
  }

}
