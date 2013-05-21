package org.sapia.ubik.rmi.server.transport.netty;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;

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
import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.concurrent.NamedThreadFactory;
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
  
  private Category             log = Log.createCategory(getClass());
  
  private NettyAddress         serverAddress;
  private ServerBootstrap      bootstrap;
  private ChannelGroup         channels = new DefaultChannelGroup();
  private ConfigurableExecutor workers;

  /**
   * @param address the {@link NettyAddress} instance corresponding to the host/port to which the server
   * should be bound.
   * @param ioConf the {@link ThreadingConfiguration} for the Netty IO processing thread pool.
   * @param workerConf the {@link ThreadingConfiguration} for the worker thread pool.
   */
  NettyServer(NettyAddress address, final MultiDispatcher dispatcher, ThreadingConfiguration ioConf, ThreadingConfiguration workerConf) {
    serverAddress = address;
    
    log.info("Initializing Netty server %s", serverAddress);
    log.info("IO thread pool config: %s", ioConf);
    log.info("Worker thread pool config: %s", workerConf);
    
    NamedThreadFactory ioThreadFactory     = NamedThreadFactory.createWith("ubik.rmi.netty.server.IO").setDaemon(true);
    NamedThreadFactory bossThreadFactory   = NamedThreadFactory.createWith("ubik.rmi.netty.server.Boss").setDaemon(true);
    NamedThreadFactory workerThreadFactory = NamedThreadFactory.createWith("ubik.rmi.netty.server.Worker").setDaemon(true);
    
    workers = new ConfigurableExecutor(workerConf, workerThreadFactory);

    ChannelFactory  factory = new NioServerSocketChannelFactory(
        new ConfigurableExecutor(ThreadingConfiguration.newInstance().setCorePoolSize(1).setMaxPoolSize(Integer.MAX_VALUE), bossThreadFactory),
        new ConfigurableExecutor(ioConf, ioThreadFactory) 
    );
    
    bootstrap = new ServerBootstrap(factory);
        
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        public ChannelPipeline getPipeline() {
            return Channels.pipeline(
                new SimpleChannelHandler() {
                  @Override
                  public void channelConnected(ChannelHandlerContext ctx,
                      ChannelStateEvent event) throws Exception {
                    channels.add(event.getChannel());
                  }
                },
                new NettyRequestDecoder(NettyServer.class.getName() + ".Decoder"),
                new NettyRmiMessageEncoder(NettyServer.class.getName() + ".Encoder"),
                new NettyServerHandler(dispatcher, serverAddress, workers)
            );
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
    workers.shutdown();
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
