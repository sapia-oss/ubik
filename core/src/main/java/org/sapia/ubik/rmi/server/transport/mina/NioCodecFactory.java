package org.sapia.ubik.rmi.server.transport.mina;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * An instance of this class instantiates {@link NioRequestDecoder}s and
 * {@link NioResponseEncoder}s.
 * 
 * @author yduchesne
 *
 */
public class NioCodecFactory implements ProtocolCodecFactory{
  
  public static final int PREFIX_LEN = 4;
  
  public ProtocolDecoder getDecoder() throws Exception {
    return new NioRequestDecoder();
  }
  
  public ProtocolEncoder getEncoder() throws Exception {
    return new NioResponseEncoder();
  }
  
}
