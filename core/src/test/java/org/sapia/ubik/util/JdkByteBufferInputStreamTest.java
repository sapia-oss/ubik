package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;


import org.junit.Test;

public class JdkByteBufferInputStreamTest {
  
  @Test
  public void testReadInt() throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(10);
    JdkByteBufferInputStream is = new JdkByteBufferInputStream(buf);
    buf.put((byte) 1);
    buf.flip();
    int i = is.read();
    assertEquals(1, i);
  }

  @Test
  public void testReadBytes() throws Exception {
    ByteBuffer buf = ByteBuffer.allocate(10);
    byte[] bytes  = new byte[] { (byte)1, (byte)2, (byte)3  };
    buf.put(bytes);
    buf.flip();
    JdkByteBufferInputStream is = new JdkByteBufferInputStream(buf);
    byte[] read = new byte[3];
    is.read(read, 0, 3);
    assertEquals(1, read[0]);
    assertEquals(2, read[1]);
    assertEquals(3, read[2]);    
  }

}
