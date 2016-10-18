package org.sapia.ubik.rmi.server.transport.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class TestEncoderOutput implements ProtocolEncoderOutput {

  IoBuffer buf;

  public TestEncoderOutput() {
    buf = IoBuffer.allocate(512);
    buf.setAutoExpand(true);
  }

  @Override
  public WriteFuture flush() {
    return null;
  }
  
  @Override
  public void mergeAll() {    
  }
  
  @Override
  public void write(Object arg) {
    doWrite((IoBuffer) arg);
  }

  private void doWrite(IoBuffer buf) {
    this.buf.put(buf);
    this.buf.flip();
  }
}
