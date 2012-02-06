package org.sapia.ubik.util.pool;

/**
 * Thrown when an error occurs when a {@link Pool} attempts creating an object
 * intended for pooling.
 * 
 * @author yduchesne
 *
 */
public class PooledObjectCreationException extends RuntimeException {
  
  static final long serialVersionUID = 1L;
  
  public PooledObjectCreationException(Exception cause) {
    super("Could not create object", cause);
  }

}
