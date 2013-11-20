package org.sapia.ubik.rmi.interceptor;

/**
 * Signals that an interceptor could not be registered for a given event.
 * 
 * @author Yanick Duchesne
 */
public class InvalidInterceptorException extends RuntimeException {

  static final long serialVersionUID = 1L;

  private Throwable err;

  public InvalidInterceptorException(String msg) {
    super(msg);
  }

  public InvalidInterceptorException(Throwable err) {
    this.err = err;
  }

  public InvalidInterceptorException(String msg, Throwable err) {
    super(msg);
    this.err = err;
  }

  public void printStackTrace() {
    super.printStackTrace();

    if (err != null) {
      System.err.println("NESTED EXCEPTION:");
      err.printStackTrace();
    }
  }

  public void printStrackTrace(java.io.PrintStream ps) {
    super.printStackTrace(ps);

    if (err != null) {
      ps.println("NESTED EXCEPTION:");
      err.printStackTrace(ps);
    }
  }

  public void printStrackTrace(java.io.PrintWriter pw) {
    super.printStackTrace(pw);

    if (err != null) {
      pw.println("NESTED EXCEPTION:");
      err.printStackTrace(pw);
    }
  }

  public Throwable getNestedError() {
    return err;
  }
}
