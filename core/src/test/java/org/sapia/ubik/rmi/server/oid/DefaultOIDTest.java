package org.sapia.ubik.rmi.server.oid;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.ubik.util.Serialization;

public class DefaultOIDTest {
  
  @Test
  public void testEqualsObject_True() throws Exception {
    DefaultOID id = new DefaultOID();
    DefaultOID copy = (DefaultOID) Serialization.deserialize(Serialization.serialize(id));
    
    assertEquals(id, copy);
  }

  @Test
  public void testEqualsFalse_True() throws Exception {
    DefaultOID id = new DefaultOID();
    DefaultOID copy = (DefaultOID) Serialization.deserialize(Serialization.serialize(id));
    copy.resetHashCode();
    
    assertNotSame(id, copy);
  }
}
