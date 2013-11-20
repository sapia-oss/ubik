package org.sapia.ubik.log;

/**
 * This interface specifies log output behavior (i.e.: the display of log
 * statements).
 * 
 * @author yduchesne
 */
public interface LogOutput {

  /**
   * The default {@link LogOutput}, which logs to stdout.
   */
  public static class DefaultLogOutput implements LogOutput {

    @Override
    public void log(String msg) {
      System.out.println(msg);
    }

    @Override
    public void log(Throwable error) {
      error.printStackTrace();
    }

    @Override
    public void close() {
    }
  }

  /**
   * @param msg
   *          the message to log.
   */
  public void log(String msg);

  /**
   * @param error
   *          a {@link Throwable} whose stack trace should be logged.
   */
  public void log(Throwable error);

  /**
   * Closes this instance.
   */
  public void close();
}
