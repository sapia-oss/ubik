package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.ByteArrayInputStream;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.sapia.ubik.rmi.server.transport.MarshalInputStream;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;
import org.sapia.ubik.rmi.server.transport.nio.tcp.NioResponseEncoder.EncoderState;
import org.sapia.ubik.util.ByteVector;
import org.sapia.ubik.util.ByteVectorInputStream;
import org.sapia.ubik.util.ByteVectorOutputStream;

/**
 * A decoder for incoming Ubik client requests.
 * @author yduchesne
 *
 */
public class NioRequestDecoder extends CumulativeProtocolDecoder{
  
  private static final String DECODER_STATE = "DECODER_STATE";  
  
  static class DecoderState{
    
    ByteVector vector = new ByteVector(4000, 2000);
    MarshalInputStream mis;
    
  }  
  
  protected boolean doDecode(IoSession sess, ByteBuffer buf, ProtocolDecoderOutput output) throws Exception {
    
    if(buf.prefixedDataAvailable(NioCodecFactory.PREFIX_LEN)){
      int length = buf.getInt();
      byte[] bytes = new byte[length];
      buf.get(bytes);
      
      DecoderState ds = (DecoderState)sess.getAttribute(DECODER_STATE);
      if(ds == null){
        ds = new DecoderState();
        sess.setAttribute(DECODER_STATE, ds);
        
        ds.vector.clear(false);
        ds.vector.write(bytes);
        ds.vector.reset();
        ds.mis = new MarshalInputStream(new ByteVectorInputStream(ds.vector));
      }
      else{
        ds.vector.clear(false);
        ds.vector.write(bytes);
        ds.vector.reset();
      }
      
      output.write(ds.mis.readObject());
      return true;
    }
    else{
      return false;
    }
  }
  
  

}
