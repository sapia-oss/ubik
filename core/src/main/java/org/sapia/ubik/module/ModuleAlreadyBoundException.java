package org.sapia.ubik.module;

/**
 * Thrown when a {@link Module} has already been bound to a
 * {@link ModuleContainer}.
 * 
 * @author yduchesne
 * 
 */
public class ModuleAlreadyBoundException extends RuntimeException {

  static final long serialVersionUID = 1L;

  public ModuleAlreadyBoundException(String msg) {
    super(msg);
  }

}
