package org.sapia.ubik.net;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * An {@link IOException} that wraps a {@link Throwable} instance. 
 * 
 * @author Yanick Duchesne
 */
public class NestedIOException extends IOException {
  
  static final long serialVersionUID = 1L;
  
  /** The source error that is encapsulated in this composite exception. */
  private Throwable theSourceError;

  /**
   * Creates a new CompositeRuntimeException instance with the arguments passed in.
   *
   * @param aMessage The message describing the error.
   * @param aSourceError The source error to encapsulate.
   */
  public NestedIOException(String aMessage, Throwable aSourceError) {
    super(aMessage);
    theSourceError = aSourceError;
  }

  /**
   * Creates a new CompositeRuntimeException instance with the argument passed in.
   *
   * @param aMessage The message describing the error.
   */
  public NestedIOException(String aMessage) {
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
    return theSourceError;
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

    if (theSourceError != null) {
      anOutput.print("\n---> NESTED EXCEPTION IS: ");
      theSourceError.printStackTrace(anOutput);
    }
  }

  /**
   * Prints the stack trace of this composite exception to the print stream passed in.
   */
  public void printStackTrace(PrintStream anOutput) {
    super.printStackTrace(anOutput);

    if (theSourceError != null) {
      anOutput.print("\n---> NESTED EXCEPTION IS: ");
      theSourceError.printStackTrace(anOutput);
    }
  }
}
