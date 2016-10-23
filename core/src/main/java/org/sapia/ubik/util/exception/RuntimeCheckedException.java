package org.sapia.ubik.util.exception;

public class RuntimeCheckedException extends RuntimeException {
  
  public RuntimeCheckedException(Exception e) {
    super(e);
    if (e instanceof RuntimeException) {
      throw new IllegalArgumentException("Constructor expecting checked exception. Got: " + e.getClass().getName());
    } 
  }

}
