package org.sapia.ubik.net;

/**
 * Indicates that the current thread has been interrupted. This class is meant as a 
 * workaround for the {@link InterruptedException} class, which extends {@link Exception}
 * and therefore must be caught when declared in the <code>throws</code> clause of 
 * a method.
 * 
 * @author yduchesne
 *
 */
public class ThreadInterruptedException extends RuntimeException {

  static final long serialVersionUID = 5068895972202822169L;
  
}