package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.apache.mina.common.ByteBuffer;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;

import junit.framework.TestCase;

public class NioRequestDecoderTest extends TestCase {

  public NioRequestDecoderTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testDoDecode() throws Exception{
    /*
    String toEncode = "THIS_IS_A_TEST";
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    MarshalOutputStream mos = new MarshalOutputStream(bos);
    mos.writeObject(toEncode);
    mos.flush();

    byte[] bytes = bos.toByteArray();
    bos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(bos);
    dos.writeInt(bytes.length);
    dos.write(bytes);
    dos.flush();
    
    NioRequestDecoder decoder = new NioRequestDecoder();
    TestDecoderOutput out = new TestDecoderOutput();

    bytes = bos.toByteArray();
    ByteBuffer buf = ByteBuffer.allocate(4+bytes.length ,true);
    buf.put(bytes);
    buf.flip();
    decoder.doDecode(null, buf, out);
    assertEquals(toEncode, out.msg);
    */
  }

}
