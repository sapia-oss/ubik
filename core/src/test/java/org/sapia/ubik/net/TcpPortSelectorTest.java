package org.sapia.ubik.net;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Test;
import org.sapia.ubik.rmi.Consts;

public class TcpPortSelectorTest {
  
  @After
  public void tearDown() {
    System.clearProperty(Consts.TCP_PORT_RANGE);
    TcpPortSelector.assignDefaultPortRange();
  }
  
  public void testAssignDefaultPortRange() {
    System.setProperty(Consts.TCP_PORT_RANGE, "[10000-20000]");
    PortRange newRange = TcpPortSelector.assignDefaultPortRange();
    assertEquals(new PortRange(10000, 20000), newRange);
  }

  @Test
  public void testSelect() throws Exception {
    TcpPortSelector selector = new TcpPortSelector() {

      @Override
      protected boolean isTaken(int port) {
        return false;
      }

      @Override
      protected void checkAvailable(int port) throws IOException {
      }

    };

    selector.select();

  }

  @Test(expected = IOException.class)
  public void testSelectFailure() throws Exception {
    TcpPortSelector selector = new TcpPortSelector() {

      @Override
      protected boolean isTaken(int port) {
        return false;
      }

      @Override
      protected void checkAvailable(int port) throws UnknownHostException, IOException {
        throw new IOException("test");
      }

    };

    selector.select();

  }

  @Test(expected = IOException.class)
  public void testSelectConcurrentFailure() throws Exception {
    TcpPortSelector selector = new TcpPortSelector() {

      @Override
      protected boolean isTaken(int port) {
        return true;
      }

      @Override
      protected void checkAvailable(int port) throws IOException {
      }

    };

    selector.select();

  }

}
