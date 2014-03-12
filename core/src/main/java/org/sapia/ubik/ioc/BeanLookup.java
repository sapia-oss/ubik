package org.sapia.ubik.ioc;

/**
 * Abstract the application container used. This allows plugging in components that
 * implement interfaces expected by the framework.
 * <p>
 * The relevant components shall have their extension points documented, so that developers
 * know which implementations can be provided in order to extend the framework.
 *
 * @author yduchesne
 *
 */
public interface BeanLookup {

  /**
   * @param typeOf returns the bean of the given type.
   * @return the desired bean, or <code>null</code> if no such bean exists.
   */
  public <T> T getBean(Class<T> typeOf);

}
