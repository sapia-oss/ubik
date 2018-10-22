package org.sapia.ubik.mcast.control;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.UbikMetrics;

/**
 * An instance of this class holds an {@link EventChannelController} contextual
 * objects.
 * 
 * @author yduchesne
 * 
 */
public class ControllerContext {

  private EventChannelFacade      channelFacade;
  private SysClock                clock;
  private ControllerConfiguration config;
  private UbikMetrics             metrics;

  /**
   * Creates an instance of this class with the given objects.
   * 
   * @param channelFacade
   *          the {@link EventChannelFacade} corresponding to the
   *          {@link EventChannel} in the context of which this instance is
   *          created.
   * @param clock
   *          the {@link SysClock} instance to use.
   * @param conf
   *          the {@link ControllerConfiguration} to use.
   * @param metrics The ubik metrics aggregator to use. 
   */
  public ControllerContext(EventChannelFacade channelFacade, SysClock clock, ControllerConfiguration conf, UbikMetrics metrics) {
    this.channelFacade = channelFacade;
    this.clock         = clock;
    this.config        = conf;
    this.metrics       = metrics;
  }
  
  /**
   * @return this instance's configuration.
   */
  public ControllerConfiguration getConfig() {
    return config;
  }

  /**
   * @return the identifier of the node of this instance's related event
   *         channel.
   * @see EventChannel#getNode()
   */
  public String getNode() {
    return channelFacade.getNode();
  }

  /**
   * @return the {@link EventChannelFacade} to which this instance corresponds.
   */
  public EventChannelFacade getEventChannel() {
    return channelFacade;
  }

  /**
   * @return the {@link SysClock} that this instance uses.
   */
  public SysClock getClock() {
    return clock;
  }

  /**
   * @return the {@link UbikMetrics} of this context.
   */
  public UbikMetrics getMetrics() {
    return metrics;
  }
  
}
