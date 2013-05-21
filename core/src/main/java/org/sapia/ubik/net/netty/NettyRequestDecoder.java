package org.sapia.ubik.net.netty;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.util.Props;

/**
 * A {@link FrameDecoder} that handles the unmarshalling of {@link NettyResponse}s.
 * 
 * @author yduchesne
 *
 */
public class NettyRequestDecoder extends FrameDecoder {

  // ==========================================================================
  // inner classes
  
  private enum DecodingStatus {
    INITIAL,
    HEADER_RECEIVED,
    PAYLOAD_RECEIVED,
    PROCESSED,
  }
  
  // --------------------------------------------------------------------------
  
  private static class DecoderState {
    private DecodingStatus    status = DecodingStatus.INITIAL;
    private int               payloadSize;
    private ChannelBuffer     incoming;
    private ObjectInputStream stream;
    
    private DecoderState() throws IOException {
      incoming = ChannelBuffers.dynamicBuffer(BUFSIZE);
    }
    
    private void reset() {
      status = DecodingStatus.INITIAL;
      incoming.clear();
    }
    
    private void headerReceived(int payloadSize) {
      this.payloadSize = payloadSize;
      status = DecodingStatus.HEADER_RECEIVED;
    }
    
    private void payloadReceived() {
      status = DecodingStatus.PAYLOAD_RECEIVED;
    }

    private void processed() {
      status = DecodingStatus.PROCESSED;
    }    
    
    private ObjectInputStream getObjectInputStream() throws IOException {
      if(stream == null) {
        stream = MarshalStreamFactory.createInputStream(new ChannelBufferInputStream(incoming));
      }
      return stream;
    }
    
  }  
  
  // ==========================================================================
  // class variables

  private static int BUFSIZE = Props.getSystemProperties().getIntProperty(
                                Consts.MARSHALLING_BUFSIZE, 
                                Consts.DEFAULT_MARSHALLING_BUFSIZE
                             );
  
  // ==========================================================================
  
  private Category log = Log.createCategory(getClass());
  
  public NettyRequestDecoder() {
  }
  
  public NettyRequestDecoder(String loggerName) {
    log = Log.createCategory(loggerName);
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent evt)
      throws Exception {
    if (log.isInfo()) {
      log.info("Caught error: %s", evt.getCause(), evt.getCause().getMessage());
    }
    ctx.sendUpstream(evt);
  }

  @Override
  protected Object decode(ChannelHandlerContext context, Channel channel, ChannelBuffer buf) throws Exception {
    
    if(buf.readableBytes() >= NettyResponse.PREFIX_LEN){
      DecoderState ds = (DecoderState) context.getAttachment();
      if(ds == null){
        ds = new DecoderState();
        context.setAttachment(ds);
      } 

      switch (ds.status) {
        case PROCESSED:
           ds.reset();
        case INITIAL:
           ds.headerReceived(buf.readInt());
        case HEADER_RECEIVED:
          if(buf.readableBytes() >= ds.payloadSize) {
            byte[] payload = new byte[ds.payloadSize];
            buf.readBytes(payload);
            ds.payloadReceived();
            ds.incoming.writeBytes(payload);
            Object obj = ds.getObjectInputStream().readObject();
            ds.processed();
            return obj;
          }
        default: // can only be PAYLOAD_RECEIVED
          return null;
      }
    }
    else{
      return null;
    }    
  }

}
