package org.sapia.ubik.module;

/**
 * Thrown when a {@link Module} could not be found.
 * 
 * @author yduchesne
 *
 */
public class ModuleNotFoundException extends RuntimeException {
  
  static final long serialVersionUID = 1L;
  
  public ModuleNotFoundException(String name) {
    super(name);
  }

}
