package org.sapia.ubik.rmi.server.transport.mina;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.Counter;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.PropUtil;

public class MinaServerExporterTest {

  @Before
  public void setUp() {
    PropUtil.clearUbikSystemProperties();
    Hub.shutdown();
    System.setProperty(Consts.COLOCATED_CALLS_ENABLED, "false");
  }

  @After
  public void tearDown() {
    PropUtil.clearUbikSystemProperties();
    Hub.shutdown();
  }

  @Test
  public void testExport() throws Exception {

    final Counter counter = new Counter(2);
    MinaServerExporter exporter = new MinaServerExporter();
    TestInterface remoteObject = (TestInterface) exporter.export(new TestInterface() {
      @Override
      public void test() {
        counter.increment();
      }
    });

    remoteObject.test();
    remoteObject.test();
    assertEquals(2, counter.getCount());
  }

  public interface TestInterface {

    public void test();
  }

}
