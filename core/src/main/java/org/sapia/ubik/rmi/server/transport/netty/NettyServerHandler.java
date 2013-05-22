package org.sapia.ubik.rmi.server.transport.netty;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.Request;
import org.sapia.ubik.net.netty.NettyAddress;
import org.sapia.ubik.net.netty.NettyResponse;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.transport.CommandHandler;

/**
 * The server-side handler, which dispatches incoming {@link NettyResponse}s.
 * 
 * @author yduchesne
 *
 */
class NettyServerHandler extends SimpleChannelHandler {

  private Category             log      = Log.createCategory(getClass());
  private NettyAddress         addr;
  private CommandHandler       handler;
  private ConfigurableExecutor workers;
  
  /**
   * @param dispatcher the {@link MultiDispatcher} instance used to dispatch remote method calls.
   * @param addr the {@link NettyResponse} of the server to which this instance belongs.
   * @param workers the {@link ConfigurableExecutor} holding the worker threads used to process incoming {@link NettyResponse}s.
   */
  NettyServerHandler(MultiDispatcher dispatcher, NettyAddress addr, ConfigurableExecutor workers){
    this.addr    = addr;
    this.handler = new CommandHandler(dispatcher, getClass());
    this.workers = workers;
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
      throws Exception {
    log.debug("exceptionCaught()", e.getCause());
  }
    
  @Override
  public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent event)
      throws Exception {
    try {
      doMessageReceived(ctx, event);
    } catch (ClosedChannelException e) {
      log.info("Channel closed, client probably closed connection", e);      
    } catch (Exception e) {
      log.warning("Could not submit request to worker pool", e);      
    }
  }
  public void doMessageReceived(final ChannelHandlerContext ctx, final MessageEvent event) throws Exception {
    workers.submit(new Runnable() {
      @Override
      public void run() {
        Object msg  = event.getMessage();

        InetSocketAddress remoteAddr = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
        log.debug(getClass(), "Handling request from %s:%s", remoteAddr.getHostString(), remoteAddr.getPort());
        final NettyRmiServerConnection conn = new NettyRmiServerConnection(new NettyAddress(remoteAddr.getHostString(), remoteAddr.getPort()), msg);
        Request req  = new Request(conn, addr);

        RMICommand cmd;
        
        try {
          cmd = (RMICommand) req.getConnection().receive();
        } catch (Exception e) {
          log.error("Could not handle request", e);
          return;
        }

        log.debug("Command received: %s from %s@%s", 
                  cmd, 
                  req.getConnection().getServerAddress(), 
                  cmd.getVmId());
        
        cmd.init(new Config(req.getServerAddress(), req.getConnection()));
        handler.handleCommand(cmd, req.getConnection());
        
        log.debug("Command handling completed: %s from %s@%s",
                  cmd.getClass().getName(), 
                  req.getConnection().getServerAddress(), 
                  cmd.getVmId());        
        
        ChannelFuture future = ctx.getChannel().write(conn.getResponse());
        future.addListener(new ChannelFutureListener() {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
              log.info("Could not write back response %s", future.getCause(), conn.getResponse());
            }
          }
        });
      }
    });
  }
}
