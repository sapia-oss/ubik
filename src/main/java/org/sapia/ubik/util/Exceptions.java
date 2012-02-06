package org.sapia.ubik.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * Exception-related methods.
 * 
 * @author yduchesne
 *
 */
public final class Exceptions {
  
  private Exceptions() {
  }
  
  /**
   * Returns the given error's stack trace as a string.
   * 
   * @param err a {@link Throwable}.
   * @return a stack trace, as a {@link String}.
   */
  public static String stackTraceToString(Throwable err) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bos);
    err.printStackTrace(writer);
    return new String(bos.toString());
  }
}
