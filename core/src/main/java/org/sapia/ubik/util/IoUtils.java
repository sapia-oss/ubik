package org.sapia.ubik.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Provides I/O related utilities.
 * 
 * @author yduchesne
 *
 */
public class IoUtils {

  private IoUtils() {
    
  }
  
  /**
   * This method calls the {@link Closeable#close()} method on the given resource if it is not <code>null</code>,
   * or does nothing otherwise. Any {@link IOException} thrown is caught silently.
   * 
   * @param resource a {@link Closeable} to close.
   */
  public static final void closeSilently(Closeable resource) {
    try {
      if (resource != null) {
        resource.close();
      }
    } catch (IOException e) {
      // noop
    }
  }
}
