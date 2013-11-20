package org.sapia.ubik.rmi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows specifying if remote method calls on instances of a
 * given remote interface should use callbacks.
 * 
 * @author yduchesne
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Callback {

}
