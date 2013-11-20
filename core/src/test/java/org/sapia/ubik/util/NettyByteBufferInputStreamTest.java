package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

public class NettyByteBufferInputStreamTest {

  @Test
  public void testReadInt() throws IOException {
    ChannelBuffer buf = ChannelBuffers.buffer(10);
    NettyByteBufferInputStream is = new NettyByteBufferInputStream(buf);
    buf.writeByte((byte) 1);
    int i = is.read();
    assertEquals(1, i);
  }

  @Test
  public void testReadBytes() throws Exception {
    ChannelBuffer buf = ChannelBuffers.buffer(10);
    byte[] bytes = new byte[] { (byte) 1, (byte) 2, (byte) 3 };
    buf.writeBytes(bytes);
    NettyByteBufferInputStream is = new NettyByteBufferInputStream(buf);
    byte[] read = new byte[3];
    is.read(read, 0, 3);
    assertEquals(1, read[0]);
    assertEquals(2, read[1]);
    assertEquals(3, read[2]);
  }

}
