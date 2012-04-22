package org.sapia.ubik.rmi.server.stats;

import org.sapia.ubik.util.Clock;

/**
 * A {@link Statistic} that encapsulates a thread-local {@link Timer}.
 * 
 * @author yduchesne
 *
 */
public class TimerStatistic extends Statistic implements StatCapable {
  
  private Timer timer;
  
  public TimerStatistic(Clock clock, String source, String name, String description) {
    super(source, name, description);
    timer = new Timer(this, clock);
  }
  
  public TimerStatistic(String source, String name, String description) {
    super(source, name, description);
    timer = new Timer(this, Clock.SystemClock.getInstance());
  }
  
  /**
   * @return this instance's {@link Timer}.
   */
  public Timer getTimer() {
    return timer;
  }

}
