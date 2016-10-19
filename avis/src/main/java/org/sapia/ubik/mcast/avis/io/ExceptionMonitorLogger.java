package org.sapia.ubik.mcast.avis.io;

import static org.sapia.ubik.mcast.avis.logging.Log.internalError;

import org.apache.mina.util.ExceptionMonitor;

/**
 * MINA exception monitor that routes exceptions to the log.
 * 
 * @author Matthew Phillips
 */
public class ExceptionMonitorLogger extends ExceptionMonitor
{
  public static final ExceptionMonitorLogger INSTANCE = 
    new ExceptionMonitorLogger ();

  private ExceptionMonitorLogger ()
  {
    // zip
  }
  
  @Override
  public void exceptionCaught (Throwable cause)
  {
    internalError ("Unexpected exception during IO", this, cause);
  }
}
