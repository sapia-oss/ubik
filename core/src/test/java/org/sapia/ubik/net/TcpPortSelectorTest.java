package org.sapia.ubik.net;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;

public class TcpPortSelectorTest {

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
