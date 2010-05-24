package org.sapia.ubik.rmi.server.transport.nio.tcp;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class TestEncoderOutput implements ProtocolEncoderOutput {
  
  ByteBuffer buf; 
  
  public WriteFuture flush() {
    return null;
  }
  public void mergeAll() {
  }
  public void write(ByteBuffer buf) {
    this.buf = buf;
  }

}
