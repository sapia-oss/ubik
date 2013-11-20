package org.sapia.ubik.rmi.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.module.ModuleContainer;
import org.sapia.ubik.rmi.server.gc.ServerGC;
import org.sapia.ubik.rmi.server.oid.DefaultOID;
import org.sapia.ubik.taskman.MockTaskManager;
import org.sapia.ubik.taskman.TaskManager;

/**
 * @author Yanick Duchesne
 */
public class ServerGCTest {

  private ServerGC gc;
  private ModuleContainer container;

  @Before
  public void setUp() {
    container = new ModuleContainer();
    container.bind(TaskManager.class, mock(MockTaskManager.class));
    container.bind(new ObjectTable());
    gc = new ServerGC();
    container.bind(gc);
    container.init();
    container.start();
  }

  @After
  public void tearDown() {
    container.stop();
  }

  @Test
  public void testRegisterRef() throws Exception {
    DefaultOID oid = new DefaultOID(1);
    VmId vmid = new VmId(1, 1);
    gc.registerRef(vmid, oid, "addr_1");
    assertEquals(1, gc.getRefCount(vmid, oid));
  }

  public void testUnregisterRef() throws Exception {
    DefaultOID oid = new DefaultOID(2);
    VmId vmid = new VmId(2, 2);
    gc.clear();
    gc.registerRef(vmid, oid, "addr_2");
    gc.dereference(vmid, oid);
    assertEquals(0, gc.getRefCount(vmid, oid));
  }

  public void testMultiVm() throws Exception {
    VmId vm1 = new VmId(1, 1);
    VmId vm2 = new VmId(2, 2);
    VmId vm3 = new VmId(3, 2);
    DefaultOID oid = new DefaultOID(3);
    String obj = "object3";
    gc.clear();

    gc.registerRef(vm1, oid, obj);
    assertTrue(gc.getRefCount(vm1, oid) == 1);
    gc.registerRef(vm2, oid, obj);
    assertEquals(2, gc.getRefCount(vm2, oid));
    assertEquals(1, gc.getSpecificCount(vm1, oid));
    assertEquals(1, gc.getSpecificCount(vm2, oid));

    gc.registerRef(vm3, oid, obj);
    assertEquals(3, gc.getRefCount(vm3, oid));
    assertEquals(1, gc.getSpecificCount(vm1, oid));
    assertEquals(1, gc.getSpecificCount(vm2, oid));
    assertEquals(1, gc.getSpecificCount(vm3, oid));

    gc.dereference(vm1, oid);
    assertEquals(2, gc.getRefCount(vm1, oid));
    assertEquals(0, gc.getSpecificCount(vm1, oid));
    assertEquals(1, gc.getSpecificCount(vm2, oid));
    assertEquals(1, gc.getSpecificCount(vm3, oid));
  }
}
