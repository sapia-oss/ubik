package org.sapia.ubik.util.exception;

import java.io.IOException;

/**
 * Wraps an {@link IOException} as a runtime one.
 * 
 * @author yduchesne.
 *
 */
public class RuntimeIoException extends RuntimeException {
  
  public RuntimeIoException(IOException e) {
    super(e.getMessage(), e);
  }
  
  /**
   * @return the original {@link IOException}.
   */
  public IOException getAsIoException() {
    return (IOException) super.getCause();
  }

}
