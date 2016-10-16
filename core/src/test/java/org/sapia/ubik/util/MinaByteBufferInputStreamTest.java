package org.sapia.ubik.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.junit.Test;

public class MinaByteBufferInputStreamTest {

  @Test
  public void testReadInt() throws IOException {
    IoBuffer buf = IoBuffer.allocate(10);
    MinaByteBufferInputStream is = new MinaByteBufferInputStream(buf);
    buf.put((byte) 1);
    buf.flip();
    int i = is.read();
    assertEquals(1, i);
  }

  @Test
  public void testReadBytes() throws Exception {
    IoBuffer buf = IoBuffer.allocate(10);
    byte[] bytes = new byte[] { (byte) 1, (byte) 2, (byte) 3 };
    buf.put(bytes);
    buf.flip();
    MinaByteBufferInputStream is = new MinaByteBufferInputStream(buf);
    byte[] read = new byte[3];
    is.read(read, 0, 3);
    assertEquals(1, read[0]);
    assertEquals(2, read[1]);
    assertEquals(3, read[2]);
  }

}
