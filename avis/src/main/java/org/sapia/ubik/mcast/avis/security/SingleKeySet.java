package org.sapia.ubik.mcast.avis.security;

import static org.sapia.ubik.mcast.avis.util.Collections.difference;

import java.util.HashSet;
import java.util.Set;

/**
 * A single set of keys. Can be used directly as a java.util.Set.
 *  
 * @author Matthew Phillips
 */
class SingleKeySet extends HashSet<Key> implements KeySet, Set<Key>
{
  SingleKeySet ()
  {
    super ();
  }
  
  SingleKeySet (Set<Key> keys)
  {
    super (keys);
  }

  public void add (KeySet theKeys)
    throws IllegalArgumentException
  {
    addAll ((SingleKeySet)theKeys);
  }

  public void remove (KeySet theKeys)
  {
    removeAll ((SingleKeySet)theKeys);
  }

  public boolean remove (Key key)
  {
    return remove ((Object)key);
  }
  
  public KeySet subtract (KeySet keys)
  {
    return new SingleKeySet (difference (this, (SingleKeySet)keys));
  }
}
