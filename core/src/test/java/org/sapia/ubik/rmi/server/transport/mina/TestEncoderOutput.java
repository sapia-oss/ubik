package org.sapia.ubik.rmi.server.transport.mina;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class TestEncoderOutput implements ProtocolEncoderOutput {
  
  ByteBuffer buf;
  
  public TestEncoderOutput() {
	  buf = ByteBuffer.allocate(512);
	  buf.setAutoExpand(true);
  }
  
  public WriteFuture flush() {
    return null;
  }
  
  public void mergeAll() {
  }
  
  public void write(ByteBuffer buf) {
    this.buf.put(buf);
    this.buf.flip();
  }
  
  public void close() {
  	buf.release();
  }

}
