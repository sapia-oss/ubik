package org.sapia.ubik.rmi.server.transport.netty;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
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
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.netty.NettyAddress;
import org.sapia.ubik.net.netty.NettyRequestDecoder;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.rmi.server.Server;

/**
 * A Netty-based implementation of the {@link Server} interface.
 * 
 * @author yduchesne
 * 
 */
class NettyServer implements Server {

  private Category log = Log.createCategory(getClass());

  private NettyAddress    serverAddress;
  private ServerBootstrap bootstrap;
  private ChannelGroup    channels = new DefaultChannelGroup();
  private ExecutorService workerThreads;

  /**
   * @param address
   *          the {@link NettyAddress} instance corresponding to the host/port
   *          to which the server should be bound.
   * @param selectorThreads the {@link ExecutorService} providing the inbound I/O threads.
   * @param workerThreads the {@link ExecutorService} providing the worker threads.
   */
  NettyServer(
      NettyAddress          address, 
      final MultiDispatcher dispatcher, 
      ExecutorService       selectorThreads, 
      ExecutorService       workerThreads) {
    this.serverAddress = address;
    this.workerThreads = workerThreads;

    log.info("Initializing Netty server %s", serverAddress);

    ChannelFactory factory = new NioServerSocketChannelFactory(selectorThreads, workerThreads);

    bootstrap = new ServerBootstrap(factory);

    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      public ChannelPipeline getPipeline() {
        return Channels.pipeline(new SimpleChannelHandler() {
          @Override
          public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
            channels.add(event.getChannel());
          }
        }, new NettyRequestDecoder(NettyServer.class.getName() + ".Decoder"), new NettyRmiMessageEncoder(NettyServer.class.getName() + ".Encoder"),
        new NettyServerHandler(dispatcher, serverAddress));
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
  public void start() throws RemoteException {
    log.debug("Starting server: " + serverAddress);
    Channel channel = bootstrap.bind(new InetSocketAddress(serverAddress.getHost(), serverAddress.getPort()));
    channels.add(channel);
  }

  @Override
  public ServerAddress getServerAddress() {
    return serverAddress;
  }
}
