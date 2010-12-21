package org.sapia.ubik.rmi.server;

import junit.framework.*;

import java.rmi.server.Unreferenced;

import org.sapia.ubik.rmi.NoSuchObjectException;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ObjectTableTest extends TestCase {
  public ObjectTableTest(String name) {
    super(name);
  }

  public void testRegister() throws Exception {
    OID oid = new OID(1);
    Hub.serverRuntime.objectTable.clear(oid);
    Hub.serverRuntime.objectTable.register(oid, "anObject");
    super.assertEquals(1, Hub.serverRuntime.objectTable.getRefCount(oid));
  }

  public void testRemove() {
    OID oid = new OID(1);
    Hub.serverRuntime.objectTable.clear(oid);
    Hub.serverRuntime.objectTable.register(oid, "anObject");
    super.assertTrue(Hub.serverRuntime.objectTable.remove("anObject"));
  }

  public void testRemoveForClassLoader() {
    OID oid = new OID(1);
    Hub.serverRuntime.objectTable.clear(oid);

    TestRemoteObj obj = new TestRemoteObj();
    Hub.serverRuntime.objectTable.register(oid, obj);
    super.assertTrue(Hub.serverRuntime.objectTable.remove(
        obj.getClass().getClassLoader()));
  }

  public void testUnreferenced() {
    OID oid = new OID(1);
    Hub.serverRuntime.objectTable.clear(oid);

    TestRemoteObj obj = new TestRemoteObj();
    Hub.serverRuntime.objectTable.register(oid, obj);
    Hub.serverRuntime.objectTable.dereference(oid, 1);
    super.assertTrue(obj.unreferenced);
  }

  public void testUnregister() throws Exception {
    OID oid = new OID(1);
    Hub.serverRuntime.objectTable.clear(oid);
    Hub.serverRuntime.objectTable.register(oid, "anObject");
    super.assertEquals(Hub.serverRuntime.objectTable.getRefCount(oid), 1);
    Hub.serverRuntime.objectTable.reference(oid);
    super.assertEquals(Hub.serverRuntime.objectTable.getRefCount(oid), 2);
    Hub.serverRuntime.objectTable.dereference(oid, 1);
    super.assertEquals(Hub.serverRuntime.objectTable.getRefCount(oid), 1);
    Hub.serverRuntime.objectTable.dereference(oid, 1);
    super.assertEquals(Hub.serverRuntime.objectTable.getRefCount(oid), 0);
  }

  public void testGetObjectFor() throws Exception {
    OID oid = new OID(1);
    Hub.serverRuntime.objectTable.clear(oid);

    try {
      Hub.serverRuntime.objectTable.getObjectFor(oid);
      throw new Exception("instance should be null");
    } catch (NoSuchObjectException e) {
      //ok
    }

    Hub.serverRuntime.objectTable.register(oid, "anObject");
    Hub.serverRuntime.objectTable.getObjectFor(oid);
  }

  public static class TestRemoteObj implements Unreferenced {
    boolean unreferenced;

    public void unreferenced() {
      unreferenced = true;
    }
  }
}
