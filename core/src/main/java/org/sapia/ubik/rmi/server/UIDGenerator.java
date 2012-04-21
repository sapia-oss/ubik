package org.sapia.ubik.rmi.server;


/**
 * A generator of unique longs for this VM.
 *
 * @author Yanick Duchesne
 */
public class UIDGenerator {
  
  private static long  uid    = System.currentTimeMillis();
  private static short offset;

  /**
   * @return a unique long for this VM.
   */
  public synchronized static long createUID() {
    long newUid = uid + (offset++);

    if (newUid < 0 || offset < 0) {
      uid      = System.currentTimeMillis();
      offset   = 0;
      newUid   = uid + (offset++);
    } 
    return newUid;
  }
}
