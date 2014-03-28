package org.sapia.ubik.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Random;

/**
 * An instance of this class is used to perform random port selection on a given
 * host.
 *
 * @author yduchesne
 *
 */
public class TcpPortSelector {

  private static final int MIN_PORT = 1025;
  private static final int MAX_PORT = 65535;
  private static final int MAX_ATTEMPS = 100;

  /**
   * @return the port that was selected.
   * @throws IOException
   *           if a problem occurred while acquiring the port.
   */
  public int select() throws IOException {
    int port = doSelect();
    try {
      Thread.sleep(new Random().nextInt(100));
    } catch (InterruptedException e) {
      throw new IllegalStateException("Thread interrupted");
    }
    if (isTaken(port)) {
      port = doSelect();
    }
    if (isTaken(port)) {
      throw new IOException("No port could be randomly acquired");
    }
    return port;
  }

  private int doSelect() throws IOException {
    Random rand = new Random();
    int attempts = 0;
    while (attempts < MAX_ATTEMPS) {
      int current = MIN_PORT + rand.nextInt(MAX_PORT - MIN_PORT);
      try {
        checkAvailable(current);
        return current;
      } catch (IOException e) {
        attempts++;
      }

    }
    throw new IOException("Could not acquire random port");
  }

  protected void checkAvailable(int port) throws IOException {
    ServerSocket ss = null;
    DatagramSocket ds = null;
    try {
      ss = new ServerSocket(port);
      ss.setReuseAddress(true);
      ds = new DatagramSocket(port);
      ds.setReuseAddress(true);
    } finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        } catch (IOException e) {
          // noop
        }
      }
    }
  }

  protected boolean isTaken(int port) {
    try {
      checkAvailable(port);
      return false;
    } catch (IOException e) {
      return true;
    }
  }
}
