package org.sapia.ubik.net.netty;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.util.Props;

/**
 * Encodes a {@link NettyResponse} instance so that it can be sent over the
 * wire.
 * 
 * @author yduchesne
 * 
 */
public class NettyResponseEncoder extends SimpleChannelHandler {

  private static final int BUFSIZE = Props.getSystemProperties()
      .getIntProperty(Consts.MARSHALLING_BUFSIZE,
          Consts.DEFAULT_MARSHALLING_BUFSIZE);

  private static final int BYTES_PER_INT = 4;

  // --------------------------------------------------------------------------

  private static class EncoderState {

    private ChannelBuffer outgoing;
    private ObjectOutputStream stream;

    private EncoderState() throws IOException {
      outgoing = ChannelBuffers.dynamicBuffer(BUFSIZE);
    }

    private ObjectOutputStream getObjectOutputStream() throws IOException {
      if (stream == null) {
        stream = MarshalStreamFactory
            .createOutputStream(new ChannelBufferOutputStream(outgoing));
      }
      return stream;
    }

  }

  // --------------------------------------------------------------------------
  
  private Category log = Log.createCategory(getClass());

  public NettyResponseEncoder() {
  }

  public NettyResponseEncoder(String loggerName) {
    log = Log.createCategory(loggerName);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent evt)
      throws Exception {
    log.info("Caught error: %s", evt.getCause(), evt.getCause().getMessage());
    ctx.sendUpstream(evt);
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent event)
      throws Exception {

    NettyResponse resp = (NettyResponse) event.getMessage();
    log.debug("Write requested for: %s", resp);
    
    EncoderState es = (EncoderState) ctx.getAttachment();
    if (es == null) {
      es = new EncoderState();
      ctx.setAttachment(es);
    }
    
    es.outgoing.clear();
    es.outgoing.writeInt(0); // reserve space for prefix

    ObjectOutputStream oos = es.getObjectOutputStream();
    
    setUp(oos, resp.getAssociatedVmId(), resp.getTransportType());
    
    oos.writeObject(resp.getObject());
    oos.flush();
    
    es.outgoing.setInt(0, es.outgoing.writerIndex() - BYTES_PER_INT); // setting length at reserved space
    log.debug("Performing write of %s  (%s bytes)", resp, es.outgoing.writerIndex() - BYTES_PER_INT);
    Channels.write(ctx, event.getFuture(), es.outgoing);
  }

  protected void setUp(ObjectOutputStream oos, VmId vmId, String transportType) {
  }

}
