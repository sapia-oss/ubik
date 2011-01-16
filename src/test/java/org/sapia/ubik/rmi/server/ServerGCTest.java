package org.sapia.ubik.rmi.server;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ServerGCTest extends TestCase {
  public ServerGCTest(String name) {
    super(name);
  }
  
  public void testRegisterRef() throws Exception {
    OID  oid  = new OID(1);
    VmId vmid = new VmId(1,1);
    Hub.serverRuntime.gc.clear();
    Hub.serverRuntime.gc.registerRef(vmid, oid, "addr_1");
    
    super.assertEquals(1, Hub.serverRuntime.gc.getRefCount(vmid, oid));
  }

  public void testUnregisterRef() throws Exception {
    OID  oid  = new OID(2);
    VmId vmid = new VmId(2,2);
    Hub.serverRuntime.gc.clear();
    Hub.serverRuntime.gc.registerRef(vmid, oid, "addr_2");
    Hub.serverRuntime.gc.dereference(vmid, oid);
    super.assertEquals(0, Hub.serverRuntime.gc.getRefCount(vmid, oid));
  }

  public void testMultiVm() throws Exception {
    VmId   vm1 = new VmId(1,1);
    VmId   vm2 = new VmId(2,2);
    VmId   vm3 = new VmId(3,2);
    OID    oid = new OID(3);
    String obj = "object3";
    Hub.serverRuntime.gc.clear();

    Hub.serverRuntime.gc.registerRef(vm1, oid, obj);
    super.assertTrue(Hub.serverRuntime.gc.getRefCount(vm1, oid) == 1);
    Hub.serverRuntime.gc.registerRef(vm2, oid, obj);
    super.assertEquals(2, Hub.serverRuntime.gc.getRefCount(vm2, oid));
    super.assertEquals(1, Hub.serverRuntime.gc.getSpecificCount(vm1, oid));
    super.assertEquals(1, Hub.serverRuntime.gc.getSpecificCount(vm2, oid));

    Hub.serverRuntime.gc.registerRef(vm3, oid, obj);
    super.assertEquals(3, Hub.serverRuntime.gc.getRefCount(vm3, oid));
    super.assertEquals(1, Hub.serverRuntime.gc.getSpecificCount(vm1, oid));
    super.assertEquals(1, Hub.serverRuntime.gc.getSpecificCount(vm2, oid));
    super.assertEquals(1, Hub.serverRuntime.gc.getSpecificCount(vm3, oid));

    Hub.serverRuntime.gc.dereference(vm1, oid);
    super.assertEquals(2, Hub.serverRuntime.gc.getRefCount(vm1, oid));
    super.assertEquals(0, Hub.serverRuntime.gc.getSpecificCount(vm1, oid));
    super.assertEquals(1, Hub.serverRuntime.gc.getSpecificCount(vm2, oid));
    super.assertEquals(1, Hub.serverRuntime.gc.getSpecificCount(vm3, oid));
  }
}
