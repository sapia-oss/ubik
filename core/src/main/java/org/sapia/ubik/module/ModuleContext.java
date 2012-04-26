package org.sapia.ubik.module;

import javax.management.ObjectName;

/**
 * An instance of this class provides an indirection to the {@link ModuleContainer}.
 * 
 * @author yduchesne
 *
 */
public interface ModuleContext {
  
  /**
   * Holds constants corresponding to the {@link ModuleContainer}'s
   * different possible states.
   */
  public enum State {
    CREATED,
    INITIALIZING,
    INITIALIZED,
    STARTING,
    STARTED,
    STOPPING,
    STOPPED;
  }
  
  /**
   * @return the container's {@link State}.
   */
  public State getState();

  /**
   * Looks up the module corresponding to the given class and returns it.
   * 
   * @param <T> the generic type of the module to lookup.
   * @param type the module {@link Class}.
   * @return the object that was looked up.
   * @throws ModuleNotFoundException if no such module could be found.
   */
  public <T> T lookup(Class<T> type) throws ModuleNotFoundException;
  
  /**
   * Registers an object as a MBean.
   * @param name the bean's {@link ObjectName}.
   * @param mbean the MBean {@link Object}.
   */
  public void registerMbean(Class<?> toNameFrom, Object mbean);

  /**
   * Registers an object an a MBean.
   * @param mbean the MBean {@link Object}.
   */
  public void registerMbean(Object mbean);
}
