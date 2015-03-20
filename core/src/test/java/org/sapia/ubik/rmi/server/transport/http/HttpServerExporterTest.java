package org.sapia.ubik.rmi.server.transport.http;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.concurrent.Counter;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.netty.NettyServerExporter;
import org.sapia.ubik.rmi.server.transport.netty.NettyServerExporterTest.TestInterface;
import org.sapia.ubik.util.PropUtil;

public class HttpServerExporterTest {

  @Before
  public void setUp() {
    Hub.shutdown();
    PropUtil.clearUbikSystemProperties();
  }

  @After
  public void tearDown() {
    Hub.shutdown();
    PropUtil.clearUbikSystemProperties();
  }

  @Test
  public void testExport() throws Exception {

    final Counter counter = new Counter(2);
    HttpServerExporter exporter = new HttpServerExporter();
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
