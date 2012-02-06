package org.sapia.ubik.rmi.server.transport.nio.tcp;

import junit.framework.TestCase;

public class NioResponseEncoderTest extends TestCase {

  public NioResponseEncoderTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }
  
  public void testEncode() throws Exception{
    /*
    NioResponseEncoder encoder = new NioResponseEncoder();
    
    String toEncode = "THIS_IS_A_TEST";
    NioResponse res = new NioResponse();
    res.setObject(toEncode);    
    TestEncoderOutput out = new TestEncoderOutput();
    encoder.encode(null, res, out);

    assertTrue(out.buf.prefixedDataAvailable(4));
    
    int length = out.buf.getInt();
    byte[] bytes = new byte[length];
    out.buf.get(bytes);
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    MarshalInputStream mis = new MarshalInputStream(bis);
    assertEquals(toEncode, mis.readObject());
    */
  }

}
