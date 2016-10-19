package org.sapia.ubik.mcast.avis.io.messages;

import static org.sapia.ubik.mcast.avis.security.Keys.EMPTY_KEYS;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.sapia.ubik.mcast.avis.security.Keys;

public class SecRqst extends RequestMessage<SecRply>
{
  public static final int ID = 54;
  
  public Keys addNtfnKeys;
  public Keys delNtfnKeys;
  public Keys addSubKeys;
  public Keys delSubKeys;
  
  public SecRqst ()
  {
    // make it easier for client to create and assign keys later
    this (EMPTY_KEYS, EMPTY_KEYS, EMPTY_KEYS, EMPTY_KEYS);
  }
  
  public SecRqst (Keys addNtfnKeys, Keys delNtfnKeys,
                  Keys addSubKeys, Keys delSubKeys)
  {
    super (nextXid ());
    
    this.addNtfnKeys = addNtfnKeys;
    this.delNtfnKeys = delNtfnKeys;
    this.addSubKeys = addSubKeys;
    this.delSubKeys = delSubKeys;
  }

  @Override
  public int typeId ()
  {
    return ID;
  }
  
  @Override
  public Class<SecRply> replyType ()
  {
    return SecRply.class;
  }
  
  @Override
  public void encode (IoBuffer out)
    throws ProtocolCodecException
  {
    super.encode (out);
    
    addNtfnKeys.encode (out);
    delNtfnKeys.encode (out);
    addSubKeys.encode (out);
    delSubKeys.encode (out);
  }
  
  @Override
  public void decode (IoBuffer in)
    throws ProtocolCodecException
  {
    super.decode (in);
    
    addNtfnKeys = Keys.decode (in);
    delNtfnKeys = Keys.decode (in);
    addSubKeys = Keys.decode (in);
    delSubKeys = Keys.decode (in);
  }
}
