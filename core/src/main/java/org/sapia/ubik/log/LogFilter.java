package org.sapia.ubik.log;

/**
 * Allows filtering which log statements are to be logged or not.
 * 
 * @author yduchesne
 * 
 */
public interface LogFilter {

  public static class DefaultFilter implements LogFilter {
    @Override
    public boolean accepts(String source) {
      return true;
    }
  }

  /**
   * @param source
   *          the source of the log statement.
   * 
   * @return <code>true</code> if the statement should be logged,
   *         <code>false</code> otherwise.
   */
  public boolean accepts(String source);

}
