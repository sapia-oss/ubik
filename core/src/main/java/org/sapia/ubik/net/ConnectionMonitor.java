package org.sapia.ubik.net;

import java.io.IOException;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;

/**
 * An instance of this class is instantiated to monitor a connection that is detected as being down.
 * 
 * @author yduchesne
 *
 */
public class ConnectionMonitor {
  
  /**
   * Specifies a facade fronting the connection to monitor.
   */
  public interface ConnectionFacade {
    
    /**
     * Attempts reconnecting: if no exception is thrown, the attempt should be deemed successful.
     * 
     * @throws IOException if reconnection fails.
     */
    public void tryConnection() throws IOException;
    
  }
  
  // ==========================================================================
  
  private Category log = Log.createCategory(getClass());
  private Thread   daemon;
  
  /**
   * This constructor internally starts a daemon thread that will attempt reconnecting in the background. 
   * The thread will terminate when reconnection succeeds.
   * 
   * @param name a name that identifies this instance.
   * @param connection a {@link ConnectionFacade} fronting the connection to monitor.
   * @param listener a {@link ConnectionStateListener} to notify upon reconnection.
   * @param attemptIntervalMillis the number of milliseconds to wait for between reconnection attempts.
   */
  public ConnectionMonitor(
      final String name,
      final ConnectionFacade connection,
      final ConnectionStateListener listener,
      final long attemptIntervalMillis) {
    
    log.warning("Starting monitoring of connection: " + name);
    
    daemon = new Thread(getClass().getName() + "@" + name) {
      
      @Override
      public void run() {
        while (true) {
          
          try {
            log.debug("Trying to reconnect");
            connection.tryConnection();
            log.debug("Reconnection successful for " + name);
            listener.onReconnected();
            break;
          } catch (IOException e) {
            log.debug("Reconnection attempt failed, will retry");
            // will retry
          } 
          
          try {
            Thread.sleep(attemptIntervalMillis);
          } catch (InterruptedException e) {
            break;
          }
        }
      }
    };
    daemon.setDaemon(true);
    daemon.start();
  }
  
  /**
   * Stops this instance's reconnection daemon.
   */
  public void stop() {
    if (daemon != null && daemon.isAlive()) {
      daemon.interrupt();
    }
  }

}
