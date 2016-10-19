package org.sapia.ubik.mcast.avis.io.messages;

import static org.sapia.ubik.mcast.avis.security.Keys.EMPTY_KEYS;

import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.sapia.ubik.mcast.avis.security.Keys;

public class UNotify extends Notify
{
  public static final int ID = 32;
  
  public int clientMajorVersion;
  public int clientMinorVersion;
  
  public UNotify ()
  {
    // zip
  }
  
  public UNotify (int clientMajorVersion, 
                  int clientMinorVersion,
                  Map<String, Object> attributes)
  {
    this (clientMajorVersion, clientMinorVersion, attributes,
          true, EMPTY_KEYS);
  }
  
  public UNotify (int clientMajorVersion, 
                  int clientMinorVersion,
                  Map<String, Object> attributes,
                  boolean deliverInsecure,
                  Keys keys)
  {
    super (attributes, deliverInsecure, keys);
    
    this.clientMajorVersion = clientMajorVersion;
    this.clientMinorVersion = clientMinorVersion;
  }
  
  @Override
  public int typeId ()
  {
    return ID;
  }

  @Override
  public void decode (IoBuffer in)
    throws ProtocolCodecException
  {
    clientMajorVersion = in.getInt ();
    clientMinorVersion = in.getInt ();
    
    super.decode (in);
  }

  @Override
  public void encode (IoBuffer out)
    throws ProtocolCodecException
  {
    out.putInt (clientMajorVersion);
    out.putInt (clientMinorVersion);
    
    super.encode (out);
  }
}
