package org.sapia.ubik.rmi.server.transport.nio.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.util.ByteVector;
import org.sapia.ubik.util.ByteVectorInputStream;
import org.sapia.ubik.util.Props;

/**
 * A decoder for incoming Ubik client requests.
 * 
 * @author yduchesne
 *
 */
public class NioRequestDecoder extends CumulativeProtocolDecoder{
  
  private static int bufsz = Props.getSystemProperties().getIntProperty(
                          			Consts.MARSHALLING_BUFSIZE, 
                          			Consts.DEFAULT_MARSHALLING_BUFSIZE
                          	 );
  
  private static final String DECODER_STATE = "DECODER_STATE";  
  
  static class DecoderState {
    
    ByteVector         vector;
    ObjectInputStream  ois;
    
    public DecoderState() throws IOException {
      this.vector = new ByteVector(bufsz, bufsz);
      ois = MarshalStreamFactory.createInputStream(new ByteVectorInputStream(vector));
    }
    
  }  
  
  protected boolean doDecode(IoSession sess, ByteBuffer buf, ProtocolDecoderOutput output) throws Exception {
    
    if(buf.prefixedDataAvailable(NioCodecFactory.PREFIX_LEN)){
      int length   = buf.getInt();
      byte[] bytes = new byte[length];
      buf.get(bytes);
      
      DecoderState ds = (DecoderState)sess.getAttribute(DECODER_STATE);
      if(ds == null){
        ds = new DecoderState();
        sess.setAttribute(DECODER_STATE, ds);
      } 
        
      ds.vector.clear(false);
      ds.vector.write(bytes);
      ds.vector.reset();
        
      output.write(ds.ois.readObject());
      return true;
    }
    else{
      return false;
    }
  }
  
  

}
