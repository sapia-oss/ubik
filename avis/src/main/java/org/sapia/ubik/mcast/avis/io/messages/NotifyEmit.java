package org.sapia.ubik.mcast.avis.io.messages;

import static org.sapia.ubik.mcast.avis.security.Keys.EMPTY_KEYS;

import java.util.Map;

import org.sapia.ubik.mcast.avis.security.Keys;

public class NotifyEmit extends Notify
{
  public static final int ID = 56;

  public NotifyEmit ()
  {
    super ();
  }
  
  public NotifyEmit (Object... attributes)
  {
    super (attributes);
  }

  public NotifyEmit (Map<String, Object> attributes)
  {
    this (attributes, true, EMPTY_KEYS);
  }
  
  public NotifyEmit (Map<String, Object> attributes,
                     boolean deliverInsecure,
                     Keys keys)
  {
    super (attributes, deliverInsecure, keys);
  }

  @Override
  public int typeId ()
  {
    return ID;
  }
}
