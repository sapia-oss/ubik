package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.ByteArrayOutputStream;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;

/**
 * An encoder of Ubik server responses.
 * 
 * @author yduchesne
 *
 */
public class NioResponseEncoder implements ProtocolEncoder{
  
  static class EncoderState{
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream(4000);
    MarshalOutputStream mos;
    
  }
  
  private static final String ENCODER_STATE = "ENCODER_STATE";
  
  public void encode(IoSession sess, Object toEncode, ProtocolEncoderOutput output) throws Exception {
    
    EncoderState es = (EncoderState)sess.getAttribute(ENCODER_STATE);
    if(es == null){
      es = new EncoderState();
      es.mos = new MarshalOutputStream(es.bos);
      sess.setAttribute(ENCODER_STATE, es);
    }
    else{
      es.bos.reset();
    }
    
    NioResponse resp = (NioResponse)toEncode;
    es.mos.setUp(resp.getAssociatedVmId(), resp.getTransportType());
    es.mos.writeObject(resp.getObject());
    es.mos.flush();
    byte[] toSend = es.bos.toByteArray();
    ByteBuffer buffer = ByteBuffer.allocate(toSend.length, false);
    buffer.put(toSend);
    buffer.flip();
    output.write(buffer);
  }
  
  public void dispose(IoSession arg0) throws Exception {
  }
}
