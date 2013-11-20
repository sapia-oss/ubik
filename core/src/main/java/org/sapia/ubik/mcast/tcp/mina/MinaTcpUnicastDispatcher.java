package org.sapia.ubik.mcast.tcp.mina;

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
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.tcp.BaseTcpUnicastDispatcher;
import org.sapia.ubik.net.ConnectionFactory;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TcpPortSelector;
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
  private SocketAcceptor acceptor;
  private ExecutorService executor;
  private ServerAddress address;
  private InetSocketAddress socketAddress;
  private int maxThreads;
  private int marshallingBufferSize;

  /**
   * @param consumer
   *          the {@link EventConsumer} to notify of incoming
   *          {@link RemoteEvent}s.
   * @param maxThreads
   *          the maximum number of worker threads.
   * @param marshallingBufferSize
   *          the size of the buzzer used for serializing/deserializing.
   * @throws IOException
   *           if a problem occurs attempting to acquire a network address.
   */
  public MinaTcpUnicastDispatcher(EventConsumer consumer, int maxThreads, int marshallingBufferSize) throws IOException {
    super(consumer);
    this.handler = new MinaTcpUnicastHandler(consumer);
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
    return MinaTcpUnicastAddress.TRANSPORT_TYPE;
  }

  @Override
  protected void doClose() {
    acceptor.unbind(socketAddress);
    executor.shutdown();
  }

  @Override
  protected ConnectionFactory doGetConnectionFactory(int soTimeout) {
    return new MinaTcpUnicastConnectionFactory(marshallingBufferSize);
  }

  @Override
  protected void doStart() {
    acceptor = new SocketAcceptor(Runtime.getRuntime().availableProcessors() + 1, Executors.newCachedThreadPool());
    if (maxThreads <= 0) {
      log.info("Using a cached thread pool (no max threads)");
      this.executor = Executors.newCachedThreadPool();
    } else {
      log.info("Using maximum number of threads: %s", maxThreads);
      this.executor = Executors.newFixedThreadPool(maxThreads);
    }

    acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new MinaTcpUnicastCodecFactory()));
    acceptor.getFilterChain().addLast("threads", new ExecutorFilter(executor));

    log.info("Binding to address: %s", socketAddress);
    this.address = new MinaTcpUnicastAddress(socketAddress.getAddress().getHostAddress(), socketAddress.getPort());
    try {
      acceptor.bind(socketAddress, handler);
    } catch (IOException e) {
      throw new IllegalStateException("Could not bind server to address " + socketAddress, e);
    }
  }

}
