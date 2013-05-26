package org.sapia.ubik.mcast.tcp.mina;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * An instance of this class instantiates {@link MinaTcpUnicastRequestDecoder}s and
 * {@link MinaTcpUnicastResponseEncoder}s.
 * 
 * @author yduchesne
 *
 */
public class MinaTcpUnicastCodecFactory implements ProtocolCodecFactory{
  
  public static final int PREFIX_LEN = 4;
  
  public ProtocolDecoder getDecoder() throws Exception {
    return new MinaTcpUnicastRequestDecoder();
  }
  
  public ProtocolEncoder getEncoder() throws Exception {
    return new MinaTcpUnicastResponseEncoder();
  }
  
}
