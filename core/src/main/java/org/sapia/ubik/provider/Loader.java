package org.sapia.ubik.provider;


/**
 * Implementations of this interface are mean to load provider implementations, given their interface.
 * 
 * @author yduchesne
 *
 */
public interface Loader {

  /**
   * 
   * @param interfaceClass the interface class whose implementation should be instantiated 
   * and inOitialized. 
   * @param hint a {@link String} hint, used for resolving the interface to the proper implementation class.
   * @return provider of the given type.
   */
  public <T> T load(Class<T> interfaceClass, String hint);

}
