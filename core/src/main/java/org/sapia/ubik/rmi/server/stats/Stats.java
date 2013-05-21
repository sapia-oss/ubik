package org.sapia.ubik.rmi.server.stats;

import java.util.ArrayList;
import java.util.List;

import org.javasimon.Counter;
import org.javasimon.Simon;
import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Props;

/**
 * A factory class to create {@link Stopwatch} and {@link Counter} instances.
 * 
 * @author yduchesne
 *
 */
public class Stats {

  private static final String DESCRIPTION = "desc";
  private static final String N_A = "N/A";
  private static final String UBIK_PREFIX = "Ubik.";
  
  static {
    if (Props.getSystemProperties().getBooleanProperty(Consts.STATS_ENABLED, false)) {
      SimonManager.enable();
    } 
  }
  
  private Stats() {
  }
  
  /**
   * @param source the name of the instance owning the {@link Stopwatch}. 
   * @param name a name.
   * @param desc a description.
   * @return a {@link Stopwatch}.
   */  
  public static Stopwatch createStopwatch(String source, String name, String desc) {
    Stopwatch s = SimonManager.getStopwatch(UBIK_PREFIX + source + "." + name);
    s.setAttribute(DESCRIPTION, desc);
    return s;
  }
  
  /**
   * @param source the {@link Class} of the instance owning the {@link Stopwatch}. 
   * @param name a name.
   * @param desc a description.
   * @return a {@link Stopwatch}.
   */    
  public static Stopwatch createStopwatch(Class<?> source, String name, String desc) {
    return createStopwatch(source.getSimpleName(), name, desc);

  }  

  /**
   * @param source the name of the instance owing the counter. 
   * @param name a name.
   * @param desc a description.
   * @return a {@link Counter}.
   */
  public static Counter createCounter(String source, String name, String desc) {
    Counter c = SimonManager.getCounter(UBIK_PREFIX + source + "." + name);
    c.setAttribute(DESCRIPTION, desc);
    return c;
  }
  
  /**
   * @param source the {@link Class} of the instance owning the counter. 
   * @param name a name.
   * @param desc a description.
   * @return a {@link Counter}.
   */
  public static Counter createCounter(Class<?> source, String name, String desc) {
    return createCounter(source.getSimpleName(), name, desc);
  }  
  
  /**
   * @param stat a {@link Simon} instance, corresponding to a statistic whose description should be returned.
   * @return the given instance's description.
   */
  public static String getDescription(Simon stat) {
    String desc = (String) stat.getAttribute(DESCRIPTION);
    if (desc == null) {
      desc = N_A;
    }
    return desc;
  }
  
  /**
   * @param stat a {@link Simon} instance, corresponding to a statistic to reset.
   */
  public static void reset(Simon stat) {
    if (stat.getName().startsWith(UBIK_PREFIX)) {
      stat.reset();
    }
  }
  
  /**
   * @return <code>true</code> if statistics are enabled.
   */
  public static boolean isEnabled() {
    return SimonManager.isEnabled();
  }
  
  /**
   * @return the {@link List} of {@link Simon} instances corresponding to Ubik stats.
   */
  public static List<Simon> getStats() {
    List<Simon> toReturn = new ArrayList<Simon>();
    for(String name : SimonManager.getSimonNames()) {
      if (name.startsWith(UBIK_PREFIX)) {
        Simon stat = SimonManager.getSimon(name);
        if (stat.isEnabled() && (stat instanceof Stopwatch || stat instanceof Counter)) {
          toReturn.add(stat);
        }
      }
    }
    return toReturn;
  }
  
}
