package org.sapia.ubik.mcast.avis.io.messages;

import static org.sapia.ubik.mcast.avis.io.XdrCoding.getLongArray;
import static org.sapia.ubik.mcast.avis.io.XdrCoding.getNameValues;
import static org.sapia.ubik.mcast.avis.io.XdrCoding.putLongArray;
import static org.sapia.ubik.mcast.avis.io.XdrCoding.putNameValues;

import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolCodecException;

public class NotifyDeliver extends Message
{
  public static final int ID = 57;

  public Map<String, Object> attributes;
  public long [] secureMatches;
  public long [] insecureMatches;
  
  public NotifyDeliver ()
  {
    // zip
  }
  
  public NotifyDeliver (Map<String, Object> attributes,
                        long [] secureMatches, long [] insecureMatches)
  {
    this.attributes = attributes;
    this.secureMatches = secureMatches;
    this.insecureMatches = insecureMatches;
  }

  @Override
  public int typeId ()
  {
    return ID;
  }

  @Override
  public void encode (IoBuffer out)
    throws ProtocolCodecException
  {
    putNameValues (out, attributes);
    putLongArray (out, secureMatches);
    putLongArray (out, insecureMatches);
  }

  @Override
  public void decode (IoBuffer in)
    throws ProtocolCodecException
  {
    attributes = getNameValues (in);
    secureMatches = getLongArray (in);
    insecureMatches = getLongArray (in);
  }
}
