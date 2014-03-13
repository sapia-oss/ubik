package org.sapia.ubik.mcast;

/**
 * This interface is meant to guard the <code>close()</code> method of the {@link EventChannel} class.
 * Callers should indeed not call that method directly, but rather use the {@link #close()} method
 * of this interface.
 * <p>
 * The reason is that {@link EventChannel} instances that are created are registered statically: only
 * the party that created an {@link EventChannel} should close it.
 *
 * @author yduchesne
 *
 */
public interface EventChannelRef {

  /**
   * @return the {@link EventChannel} this instance refers to.
   */
  public EventChannel get();

  /**
   * Closes the underlying {@link EventChannel} - if the implementation determines that it should.
   */
  public void close();
}
