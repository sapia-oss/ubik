package org.sapia.ubik.rmi.server;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A generator of unique longs for this VM. Note that the internal counter is
 * cyclic: it will be reset when the maximum UID will have been reached. This
 * class should thus not be used to generate IDs that are meant for persistence.
 * 
 * @author Yanick Duchesne
 */
public class UIDGenerator {

  private static final long MAX_UID = Long.MAX_VALUE - 1000;
  private static final AtomicLong uid = new AtomicLong();

  /**
   * @return a unique long for this VM.
   */
  public static long createUID() {
    long toReturn = uid.incrementAndGet();
    if (toReturn >= MAX_UID) {
      uid.compareAndSet(toReturn, 0);
    }
    return toReturn;
  }

  // for unit testing
  static void reset() {
    uid.set(0);
  }
}
