package org.sapia.ubik.concurrent;

/**
 * Wraps an {@link InterruptedException}.
 * 
 * @author yduchesne
 * 
 */
public class RuntimeInterruptedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * @param msg
   *          a custom error message.
   * @param cause
   *          the {@link InterruptedException} that was raised.
   */
  public RuntimeInterruptedException(String msg, InterruptedException cause) {
    super(msg, cause);
  }

}
