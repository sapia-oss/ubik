package org.sapia.ubik.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sapia.ubik.rmi.server.RuntimeRemoteException;

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
   * @param err
   *          a {@link Throwable}.
   * @return a stack trace, as a {@link String}.
   */
  public static String stackTraceToString(Throwable err) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bos);
    err.printStackTrace(writer);
    return new String(bos.toString());
  }

  /**
   * Fills in the given {@link Throwable} instance with the calling thread's stack trace, but
   * without replacing the stack trace it currently has (which would occur if the original
   * {@link Throwable#fillInStackTrace()} method would be invoked.
   *
   * @param toFillIn the {@link Throwable} instance whose stack trace should be filled in.
   */
  public static void fillInStackTrace(Throwable toFillIn) {
    Exception current = new Exception("e1");
    current.fillInStackTrace();
    List<StackTraceElement> elements = new ArrayList<StackTraceElement>();
    elements.addAll(Arrays.asList(toFillIn.getStackTrace()));
    List<StackTraceElement> currentStackTrace = Arrays.asList(current.getStackTrace());
    for (int i = 1; i < currentStackTrace.size(); i++) {
      elements.add(currentStackTrace.get(i));
    }
    toFillIn.setStackTrace(elements.toArray(new StackTraceElement[elements.size()]));
  }
  
  /**
   * @param e the {@link Throwable} to test.
   * @return <code>true</code> if the given exception is an instance of {@link RemoteException} or {@link RuntimeRemoteException}.
   */
  public static boolean isRemoteException(Throwable e) {
    return e instanceof RemoteException || e instanceof RuntimeRemoteException;
  }
}
