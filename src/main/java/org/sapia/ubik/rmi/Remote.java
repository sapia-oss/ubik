package org.sapia.ubik.rmi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation allows specifying remote interface directly on concrete remote classes.
 * 
 * @author yduchesne
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Remote {

  /**
   * @return the array of {@link Class} instances corresponding to the interfaces that
   * should be exported as part of remoting.
   */
  public Class<?>[] interfaces();
}
