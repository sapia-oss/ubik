package org.sapia.ubik.rmi.server.transport.nio.tcp;

import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class TestDecoderOutput implements ProtocolDecoderOutput{
  
  Object msg;
  
  public void flush() {
  }
  
  public void write(Object msg) {
    this.msg = msg;
  }

  
}
