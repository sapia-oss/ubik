package org.sapia.ubik.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides utility methods pertaining to streams.
 * 
 * @author yduchesne
 * 
 */
public final class Streams {

  private Streams() {
  }

  /**
   * This methods flushes and closes a stream and catches any thrown
   * {@link IOException} doing so. If the given stream is <code>null</code>, it
   * is simply ignored.
   * 
   * @param os
   *          an {@link OutputStream} to flush and close.
   */
  public static final void flushAndCloseSilently(OutputStream os) {
    if (os != null) {
      try {
        os.flush();
      } catch (Exception e) {
        // noop
      }
      try {
        os.close();
      } catch (Exception e) {
        // noop
      }
    }
  }

  /**
   * This methods closes a stream and catches any thrown {@link IOException}
   * doing so. If the given stream is <code>null</code>, it is simply ignored.
   * 
   * @param os
   *          an {@link OutputStream} to close.
   */
  public static final void closeSilently(OutputStream os) {
    if (os != null) {
      try {
        os.close();
      } catch (Exception e) {
        // noop
      }
    }
  }

  /**
   * This methods closes a stream and catches any thrown {@link IOException}
   * doing so. If the given stream is <code>null</code>, it is simply ignored.
   * 
   * @param is
   *          an {@link InputStream} to flush.
   */
  public static final void closeSilently(InputStream is) {
    if (is != null) {
      try {
        is.close();
      } catch (Exception e) {
        // noop
      }
    }
  }
}
