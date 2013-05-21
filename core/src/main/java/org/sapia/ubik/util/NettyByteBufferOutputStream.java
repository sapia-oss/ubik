package org.sapia.ubik.util;

import java.io.IOException;
import java.io.OutputStream;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * An {@link OutputStream} implementation that writes to an internal {@link ChannelBuffer}.
 * 
 * @author yduchesne
 *
 */
public class NettyByteBufferOutputStream  extends OutputStream {
	
  private ChannelBuffer buf;
  
  public NettyByteBufferOutputStream(ChannelBuffer buf){
    this.buf = buf;
  }
  
  public synchronized void write(int b) throws IOException {
    buf.writeByte((byte) b);
  }

  public synchronized void write(byte[] bytes, int off, int len) throws IOException {
    buf.writeBytes(bytes, off, len);
  }

}
