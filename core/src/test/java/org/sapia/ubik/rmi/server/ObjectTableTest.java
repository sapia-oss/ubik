package org.sapia.ubik.rmi.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.rmi.server.Unreferenced;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.module.TestModuleContext;
import org.sapia.ubik.rmi.NoSuchObjectException;
import org.sapia.ubik.rmi.server.oid.DefaultOID;

public class ObjectTableTest {

  private ObjectTable objectTable;

  @Before
  public void setUp() throws Exception {
    objectTable = new ObjectTable();
    TestModuleContext context = new TestModuleContext();
    objectTable.init(context);
    objectTable.start(context);
  }

  @Test
  public void testRegister() throws Exception {
    DefaultOID oid = new DefaultOID(1);
    objectTable.clear(oid);
    objectTable.register(oid, "anObject");
    assertEquals(1, objectTable.getRefCount(oid));
  }

  @Test
  public void testRemove() {
    DefaultOID oid = new DefaultOID(1);
    objectTable.clear(oid);
    objectTable.register(oid, "anObject");
    assertTrue(objectTable.remove("anObject"));
  }

  @Test
  public void testRemoveForClassLoader() {
    DefaultOID oid = new DefaultOID(1);
    objectTable.clear(oid);

    TestRemoteObj obj = new TestRemoteObj();
    objectTable.register(oid, obj);
    assertTrue(objectTable.remove(obj.getClass().getClassLoader()));
  }

  @Test
  public void testUnreferenced() {
    DefaultOID oid = new DefaultOID(1);
    objectTable.clear(oid);

    TestRemoteObj obj = new TestRemoteObj();
    objectTable.register(oid, obj);
    objectTable.dereference(oid, 1);
    assertTrue(obj.unreferenced);
  }

  @Test
  public void testUnregister() throws Exception {
    DefaultOID oid = new DefaultOID(1);
    objectTable.clear(oid);
    objectTable.register(oid, "anObject");
    assertEquals(objectTable.getRefCount(oid), 1);
    objectTable.reference(oid);
    assertEquals(objectTable.getRefCount(oid), 2);
    objectTable.dereference(oid, 1);
    assertEquals(objectTable.getRefCount(oid), 1);
    objectTable.dereference(oid, 1);
    assertEquals(objectTable.getRefCount(oid), 0);
  }

  @Test
  public void testGetObjectFor() throws Exception {
    DefaultOID oid = new DefaultOID(1);
    objectTable.clear(oid);

    try {
      objectTable.getObjectFor(oid);
      throw new Exception("NoSuchObjectException should be thrown");
    } catch (NoSuchObjectException e) {
      // ok
    }

    objectTable.register(oid, "anObject");
    objectTable.getObjectFor(oid);
  }

  public static class TestRemoteObj implements Unreferenced {
    boolean unreferenced;

    public void unreferenced() {
      unreferenced = true;
    }
  }
}
