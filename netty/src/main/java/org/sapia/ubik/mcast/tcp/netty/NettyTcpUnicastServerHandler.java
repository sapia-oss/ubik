package org.sapia.ubik.mcast.tcp.netty;

import java.util.concurrent.ExecutorService;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventConsumer;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.netty.NettyResponse;
import org.sapia.ubik.util.Func;

/**
 * This handler expects {@link RemoteEvent}s and handles them by dispatching
 * them to its {@link EventConsumer}, sending back any required synchronous
 * response.
 * 
 * @author yduchesne
 * 
 */
public class NettyTcpUnicastServerHandler extends SimpleChannelHandler {

  private Category                  log = Log.createCategory(getClass());
  private EventConsumer             consumer;
  private ExecutorService           workers;
  private Func<ServerAddress, Void> addressSupplier;

  /**
   * @param consumer
   *          the {@link EventConsumer} to notify.
   * @param workers
   *          the {@link ExecutorService} providing worker threads to this
   *          instance.
   * @param addressSupplier
   *          the function providing the {@link ServerAddress} of the server in the context of which this instance
   *          is used.
   */
  public NettyTcpUnicastServerHandler(EventConsumer consumer, ExecutorService workers, Func<ServerAddress, Void> addressSupplier) {
    this.consumer        = consumer;
    this.workers         = workers;
    this.addressSupplier = addressSupplier;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    log.error("Exception caught", e.getCause());
    ctx.getChannel().close();
  }

  @Override
  public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent msg) throws Exception {
    try {
      doMessageReceived(ctx, msg);
    } catch (RuntimeException e) {
      log.warning("Could not submit request to worker pool", e);
    }
  }

  private void doMessageReceived(final ChannelHandlerContext ctx, final MessageEvent msg) {
    workers.submit(new Runnable() {
      @Override
      public void run() {
        try {
          Object incoming = msg.getMessage();
          if (incoming instanceof RemoteEvent) {
            RemoteEvent evt = (RemoteEvent) incoming;

            if (evt.isSync()) {
              if (consumer.hasSyncListener(evt.getType())) {
                log.debug("Received sync remote event %s from %s, notifying listener", evt.getType(), evt.getNode());
                Object response = consumer.onSyncEvent(evt);
                ctx.getChannel().write(new NettyResponse(new Response(addressSupplier.call(null), evt.getId(), response)));
              } else {
                log.debug("Received sync remote event %s from %s, no listener to notify", evt.getType(), evt.getNode());
                ctx.getChannel().write(new NettyResponse(new Response(addressSupplier.call(null), evt.getId(), null).setNone()));
              }
            } else {
              log.debug("Received async remote event %s from %s, notifying listeners", evt.getType(), evt.getNode());
              consumer.onAsyncEvent(evt);
            }
          } else {
            log.error("Object not a remote event: " + incoming.getClass().getName() + "; " + msg);
          }
        } catch (Exception e) {
          log.error("Error caught handling request", e);
        }
      }
    });

  }

}
