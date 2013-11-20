package org.sapia.ubik.net;

/**
 * Thrown when a URI could not be parsed.
 * 
 * @see Uri
 * 
 * @author Yanick Duchesne
 */
public class UriSyntaxException extends RuntimeException {

  static final long serialVersionUID = 1L;

  /**
   * Constructor for UriSyntaxException.
   */
  public UriSyntaxException(String msg) {
    super(msg);
  }
}
