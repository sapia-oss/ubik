package org.sapia.ubik.rmi;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RmiIOException extends IOException {
  
  static final long serialVersionUID = 1L;
  
  /** The source error that is encapsulated in this composite exception. */
  private Throwable _theSourceError;

  /**
   * Creates a new CompositeRuntimeException instance with the arguments passed in.
   *
   * @param aMessage The message describing the error.
   * @param aSourceError The source error to encapsulate.
   */
  public RmiIOException(String aMessage, Throwable aSourceError) {
    super(aMessage);
    _theSourceError = aSourceError;
  }

  /**
   * Creates a new CompositeRuntimeException instance with the argument passed in.
   *
   * @param aMessage The message describing the error.
   */
  public RmiIOException(String aMessage) {
    super(aMessage);
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////  ACCESSOR METHODS  ///////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the source error encapsulated in this composite exception.
   *
   * @return The source error encapsulated in this composite exception.
   */
  public Throwable getSourceError() {
    return _theSourceError;
  }

  /////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////  OVERRIDEN METHODS  //////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Prints the stack trace of this composite exception to the standard error stream.
   */
  public void printStackTrace() {
    printStackTrace(System.err);
  }

  /**
   * Prints the stack trace of this composite exception to the print writer passed in.
   */
  public void printStackTrace(PrintWriter anOutput) {
    super.printStackTrace(anOutput);

    if (_theSourceError != null) {
      anOutput.print("\n---> NESTED EXCEPTION IS: ");
      _theSourceError.printStackTrace(anOutput);
    }
  }

  /**
   * Prints the stack trace of this composite exception to the print stream passed in.
   */
  public void printStackTrace(PrintStream anOutput) {
    super.printStackTrace(anOutput);

    if (_theSourceError != null) {
      anOutput.print("\n---> NESTED EXCEPTION IS: ");
      _theSourceError.printStackTrace(anOutput);
    }
  }
}
