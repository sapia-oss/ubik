package org.sapia.ubik.net.netty;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Implements an {@link InputStream} over a {@link ChannelBuffer}.
 * 
 * @author yduchesne
 * 
 */
public class NettyByteBufferInputStream extends InputStream {

  private ChannelBuffer buf;

  public NettyByteBufferInputStream(ChannelBuffer buf) {
    this.buf = buf;
  }

  @Override
  public int available() throws IOException {
    return buf.readableBytes();
  }

  public synchronized int read() throws IOException {
    if (buf.readableBytes() <= 0) {
      return -1;
    }
    return buf.readByte() & 0xFF;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  public synchronized int read(byte[] bytes, int off, int len) throws IOException {
    if (buf.readableBytes() <= 0) {
      return -1;
    }
    len = Math.min(len, buf.readableBytes());
    buf.readBytes(bytes, off, len);

    return len;
  }

}
