package org.sapia.ubik.net;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import org.junit.Test;

public class TcpPortSelectorTest {

  @Test
  public void testSelect() throws Exception {
    TcpPortSelector selector = new TcpPortSelector() {

      @Override
      protected boolean isTaken(String host, int port) {
        return false;
      }

      @Override
      protected void checkAvailable(String host, int port) throws UnknownHostException, IOException {
        throw new ConnectException();
      }

    };

    selector.select("test");

  }

  @Test(expected = IOException.class)
  public void testSelectFailure() throws Exception {
    TcpPortSelector selector = new TcpPortSelector() {

      @Override
      protected boolean isTaken(String host, int port) {
        return false;
      }

      @Override
      protected void checkAvailable(String host, int port) throws UnknownHostException, IOException {
      }

    };

    selector.select("test");

  }

  @Test(expected = IOException.class)
  public void testSelectConcurrentFailure() throws Exception {
    TcpPortSelector selector = new TcpPortSelector() {

      @Override
      protected boolean isTaken(String host, int port) {
        return true;
      }

      @Override
      protected void checkAvailable(String host, int port) throws UnknownHostException, IOException {
        throw new ConnectException();
      }

    };

    selector.select("test");

  }

}
