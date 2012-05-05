package org.sapia.ubik.mcast.tcp;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.serialization.SerializationStreams;
import org.sapia.ubik.util.ByteBufferOutputStream;
import org.sapia.ubik.util.Props;

/**
 * An encoder of unicast responses.
 * 
 * @author yduchesne
 *
 */
public class NioTcpUnicastResponseEncoder implements ProtocolEncoder {
	
  private static final int BUFSIZE = Props.getSystemProperties().getIntProperty(
                                  			Consts.MARSHALLING_BUFSIZE, 
                                  			Consts.DEFAULT_MARSHALLING_BUFSIZE
                                  	 );

  private static final String ENCODER_STATE = "ENCODER_STATE";
  
  private static final int BYTES_PER_INT = 4;

  // --------------------------------------------------------------------------
  
  private static class EncoderState {
  	
    private ByteBuffer 				 outgoing;
    
    private EncoderState() throws IOException {
    	outgoing = ByteBuffer.allocate(BUFSIZE);
    	outgoing.setAutoExpand(true);
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  public void encode(IoSession sess, Object toEncode, ProtocolEncoderOutput output) throws Exception {
    
    EncoderState es = (EncoderState)sess.getAttribute(ENCODER_STATE);
    if (es == null) {
      es = new EncoderState();
      sess.setAttribute(ENCODER_STATE, es);
    }
    es.outgoing.clear();
    es.outgoing.putInt(0); // reserve space for length header
    doEncode(toEncode, es.outgoing, output);
  }
  
  void doEncode(Object toEncode, ByteBuffer outputBuffer, ProtocolEncoderOutput output) throws Exception {
    ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(new ByteBufferOutputStream(outputBuffer));
    oos.writeObject(toEncode);
    oos.flush();
    oos.close();
    outputBuffer.putInt(0, outputBuffer.position() - BYTES_PER_INT); // setting length at reserved space
    outputBuffer.flip();
    output.write(outputBuffer);  	
  }
  
  public void dispose(IoSession sess) throws Exception {
    EncoderState es = (EncoderState)sess.getAttribute(ENCODER_STATE);  	
    if (es != null) {
      es.outgoing.release();
    }
  }
}
