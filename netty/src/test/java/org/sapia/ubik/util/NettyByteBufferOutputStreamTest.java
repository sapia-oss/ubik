package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.sapia.ubik.net.netty.NettyByteBufferOutputStream;

public class NettyByteBufferOutputStreamTest {

  @Test
  public void testWriteInt() throws IOException {
    ChannelBuffer buf = ChannelBuffers.buffer(10);
    NettyByteBufferOutputStream os = new NettyByteBufferOutputStream(buf);
    os.write(1);
    int i = buf.readByte();
    assertEquals(1, i);
  }

  @Test
  public void testWriteByteArray() throws IOException {
    ChannelBuffer buf = ChannelBuffers.buffer(10);
    NettyByteBufferOutputStream os = new NettyByteBufferOutputStream(buf);
    byte[] bytes = new byte[] { (byte) 1, (byte) 2, (byte) 3 };
    os.write(bytes);
    byte[] read = new byte[3];
    buf.readBytes(read);
    assertEquals(1, read[0]);
    assertEquals(2, read[1]);
    assertEquals(3, read[2]);
  }

  @Test
  public void testWriteByteArrayWithStartAndOffset() throws IOException {
    ChannelBuffer buf = ChannelBuffers.buffer(10);
    NettyByteBufferOutputStream os = new NettyByteBufferOutputStream(buf);
    byte[] bytes = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, };
    os.write(bytes);
    byte[] read = new byte[3];
    buf.readBytes(read, 0, 3);
    assertEquals(1, read[0]);
    assertEquals(2, read[1]);
    assertEquals(3, read[2]);
  }

}
