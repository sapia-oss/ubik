package org.sapia.ubik.rmi.server.transport.mina;

import org.apache.mina.core.filterchain.IoFilter.NextFilter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class TestDecoderOutput implements ProtocolDecoderOutput {

  Object msg;

  @Override
  public void flush(NextFilter arg0, IoSession arg1) {
    
  }
  
  public void write(Object msg) {
    this.msg = msg;
  }

}
