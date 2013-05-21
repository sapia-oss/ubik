package org.sapia.ubik.util;

import static org.junit.Assert.*;


import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

public class JdkByteBufferOutputStreamTest {

  @Test
  public void testWriteInt() throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(10);
    JdkByteBufferOutputStream os = new JdkByteBufferOutputStream(buf);
    os.write(1);
    buf.flip();
    int i = buf.get();
    assertEquals(1, i);
  }

  @Test
  public void testWriteByteArray() throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(10);
    JdkByteBufferOutputStream os = new JdkByteBufferOutputStream(buf);
    byte[] bytes  = new byte[] { (byte)1, (byte)2, (byte)3 };
    os.write(bytes);
    buf.flip();
    byte[] read = new byte[3];
    buf.get(read);
    assertEquals(1, read[0]);
    assertEquals(2, read[1]);
    assertEquals(3, read[2]);    
  }
  
  @Test
  public void testWriteByteArrayWithStartAndOffset() throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(10);
    JdkByteBufferOutputStream os = new JdkByteBufferOutputStream(buf);
    byte[] bytes  = new byte[] { (byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6,};
    os.write(bytes);
    buf.flip();
    byte[] read = new byte[3];
    buf.get(read, 0, 3);
    assertEquals(1, read[0]);
    assertEquals(2, read[1]);
    assertEquals(3, read[2]);    
  }  

}
