package org.sapia.ubik.mcast.hazelcast;

import org.sapia.ubik.util.Assertions;

import com.hazelcast.core.HazelcastInstance;

/**
 * Gives access to an {@link HazelcastInstance} which will internally be used by Ubik's
 * components, if required.
 * <p>
 * It is the responsability of applications to set that instance on this class,
 * using the {@link Singleton#set(HazelcastInstance)} method.
 * 
 * @author yduchesne
 *
 */
public class Singleton {
  
  private static HazelcastInstance singleton;
  
  /**
   * @param instance the {@link HazelcastInstance} to use.
   */
  public static void set(HazelcastInstance instance) {
    Assertions.illegalState(singleton != null, "HazelcastInstance already set");
    singleton = instance;
  }
  
  /**
   * Internally sets the {@link HazelcastInstance} to <code>null</code> - IMPORTANT: the instance is not shut down.
   * It is the application's responsibility to do so.
   */
  public static void unset() {
    singleton = null;
  }
  
  /**
   * @return the {@link HazelcastInstance} to use.
   */
  public static HazelcastInstance get() {
    Assertions.illegalState(
        singleton == null, 
        "HazelcastInstance not set: call set on %s to set the instance to use", 
        Singleton.class.getName()
    );
    return singleton;
  }

}
