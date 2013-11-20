package org.sapia.ubik.rmi.server.transport.netty;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.Counter;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.PropertiesUtil;

public class NettyServerExporterTest {

  @Before
  public void setUp() {
    Hub.shutdown();
    PropertiesUtil.clearUbikSystemProperties();
  }

  @After
  public void tearDown() {
    Hub.shutdown();
    PropertiesUtil.clearUbikSystemProperties();
  }

  @Test
  public void testExport() throws Exception {

    final Counter counter = new Counter(2);
    NettyServerExporter exporter = new NettyServerExporter();
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
