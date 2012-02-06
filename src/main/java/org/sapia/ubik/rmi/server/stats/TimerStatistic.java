package org.sapia.ubik.rmi.server.stats;

/**
 * A {@link Statistic} that encapsulates a thread-local {@link Timer}.
 * 
 * @author yduchesne
 *
 */
public class TimerStatistic extends Statistic implements StatCapable {
  
  private Timer timer;
  
  public TimerStatistic(String source, String name, String description) {
    super(source, name, description);
    timer = new Timer(this);
  }
  
  /**
   * @return this instance's {@link Timer}.
   */
  public Timer getTimer() {
    return timer;
  }

}
