package org.sapia.ubik.rmi.server.gc;

import java.util.Date;

import org.sapia.ubik.rmi.Consts;

public interface ClientGCMBean {

  /**
   * @see Consts#CLIENT_GC_INTERVAL
   */
  public long getInterval();
  
  /**
   * @see Consts#CLIENT_GC_BATCHSIZE
   */  
  public int getBatchSize();
  
  public void setBatchSize(int batchSize);

  /**
   * @see Consts#CLIENT_GC_THRESHOLD
   */
  public int getThreshold();
  
  public void setThreshold(int t);
  
  /**
   * @return the current number of remote objects referred to by the client GC.
   */
  public int getRemoteObjectCount();
  
  /**
   * @return the {@link Date} at which the Client GC last contacted the server(s).
   */
  public Date getLastGcTime();
  
  /**
   * @return the number of remote objects that were GC'ed at the last run.
   */
  public int getLastGcCount();

  /**
   * @return the number of remote objects that were cleaned up by the Client GC.
   */
  public double getNumGc();
  
  /**
   * @return the number times explicit GC has been triggered.
   * @see #getThreshold()
   */
  public double getForcedGc();  
}
