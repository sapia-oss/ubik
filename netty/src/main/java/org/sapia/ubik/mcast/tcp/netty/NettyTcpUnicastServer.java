package org.sapia.ubik.mcast.tcp.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.ChannelGroupFutureListener;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.netty.NettyRequestDecoder;
import org.sapia.ubik.rmi.server.Server;
import org.sapia.ubik.rmi.server.transport.netty.NettyRmiMessageEncoder;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Func;

/**
 * A Netty-based implementation of the {@link Server} interface.
 * 
 * @author yduchesne
 * 
 */
class NettyTcpUnicastServer implements Server {

  private Category               log = Log.createCategory(getClass());
  private NettyTcpUnicastAddress serverAddress;
  private ServerBootstrap        bootstrap;
  private ChannelGroup           channels = new DefaultChannelGroup();
  private ExecutorService        workerThreads;

  /**
   * @param the
   *          {@link EventConsumer} to which this instance will dispatch
   *          incoming remote events.
   * @param address
   *          the {@link NettyTcpUnicastAddress} instance corresponding to the
   *          host/port to which the server should be bound.
   * @param selectorThreads
   *          the {@link ExecutorService} providing the NIO selector threads.
   * @param workerThreads
   *          the {@link ExecutorService} providing the worker threads.
   * @param nSelectors 
   *          the actual number of threads to create using the provided selector 
   *          thread pool.          
   */
  NettyTcpUnicastServer(
      EventConsumer consumer, 
      NettyTcpUnicastAddress address, 
      ExecutorService selectorThreads, 
      ExecutorService workerThreads, 
      int nSelectors) {
    this.serverAddress = address;
    this.workerThreads = workerThreads;
    log.debug("Initializing Netty server %s", serverAddress);

    ChannelFactory factory = new NioServerSocketChannelFactory(selectorThreads, workerThreads, nSelectors);

    bootstrap = new ServerBootstrap(factory);

    final NettyTcpUnicastServerHandler handler = new NettyTcpUnicastServerHandler(
        consumer, 
        workerThreads,
        new Func<ServerAddress, Void>() {
          @Override
          public ServerAddress call(Void arg) {
            Assertions.illegalState(serverAddress == null, "Server address not set");
            return serverAddress;
          }
        }
    );
    
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() {
        return Channels.pipeline(new SimpleChannelHandler() {
          @Override
          public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
            channels.add(event.getChannel());
          }
        }, new NettyRequestDecoder(), new NettyRmiMessageEncoder(), handler);
      }
    });
    bootstrap.setOption("child.tcpNoDelay", true);
    bootstrap.setOption("child.keepAlive", true);
  }

  @Override
  public void close() {
    log.debug("Stopping server: " + serverAddress);
    if (bootstrap != null) {
      ChannelGroupFuture future = channels.close();
      final BlockingRef<Void> shutdownRef = new BlockingRef<Void>();
      future.addListener(new ChannelGroupFutureListener() {
        @Override
        public void operationComplete(ChannelGroupFuture future) throws Exception {
          log.debug("Shutdown of channels completed");
          shutdownRef.setNull();
        }
      });
      try {
        shutdownRef.await();
      } catch (InterruptedException e) {
        // noop
      }
    }
    workerThreads.shutdown();
    log.debug("Stopped server: " + serverAddress);
  }

  @Override
  public void start() {
    log.debug("Starting server: " + serverAddress);
    Channel channel = bootstrap.bind(new InetSocketAddress(serverAddress.getHost(), serverAddress.getPort()));
    channels.add(channel);
  }

  @Override
  public ServerAddress getServerAddress() {
    return serverAddress;
  }
}
