package org.sapia.ubik.net;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Random;

import org.sapia.ubik.rmi.Consts;

/**
 * An instance of this class is used to perform random port selection on a given
 * host.
 *
 * @author yduchesne
 *
 */
public class TcpPortSelector {

  private static PortRange defaultPortRange = assignDefaultPortRange();
  private static final int MAX_ATTEMPS = 100;
  private static final int MIN_RANDOM_SLEEP = 5;
  private static final int MAX_RANDOM_SLEEP = 100;
  
  // visible for testing
  static PortRange assignDefaultPortRange() {
    defaultPortRange = PortRange.valueOf(
        System.getProperty(Consts.TCP_PORT_RANGE, Consts.DEFAULT_TCP_PORT_RANGE)
    );
    return defaultPortRange;
  }

  /**
   * Selects a port within a default random range (between 1025 and 32000).
   * 
   * @return the port that was selected.
   * @throws IOException
   *           if a problem occurred while acquiring the port.
   *           
   * @see Consts#TCP_PORT_RANGE
   * @see Consts#DEFAULT_TCP_PORT_RANGE
   */
  public int select() throws IOException {
    return select(defaultPortRange);
  }
  
  /**
   * @param range a PortRange to use.
   * @return
   * @throws IOException
   */
  public int select(PortRange range) throws IOException {
    int port = doSelect(range);
    try {
      Thread.sleep(new Random().nextInt(MAX_RANDOM_SLEEP - MIN_RANDOM_SLEEP) + MIN_RANDOM_SLEEP);
    } catch (InterruptedException e) {
      throw new IllegalStateException("Thread interrupted");
    }
    if (isTaken(port)) {
      port = doSelect(range);
    }
    if (isTaken(port)) {
      throw new IOException("No port could be randomly acquired");
    }
    return port;
    
  }
  
  // --------------------------------------------------------------------------

  private int doSelect(PortRange range) throws IOException {
    Random rand = new Random();
    int attempts = 0;
    while (attempts < MAX_ATTEMPS) {
      int current = range.getMinPort() + rand.nextInt(range.getMaxPort() - range.getMinPort());
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
