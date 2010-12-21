package org.sapia.ubik.rmi.server.perf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;

import org.sapia.ubik.jmx.MBeanContainer;
import org.sapia.ubik.jmx.MBeanFactory;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.PropUtil;


/**
 * This class is used to collect performance data (in fact, average durations) about
 * arbitrary operations - internally kept in the for of {@link Topic}s.
 * <p>
 *   Usage:
 * <pre>
 * public class MyOperation{
 * 
 *   Topic t = PerfAnalyzer.getInstance().getTopic("MyOperation.Duration");
 *   
 *   public void doSomething(){
 *     if(t.isEnabled()){
 *       t.start();
 *     }
 *     // do something
 *     
 *     if(t.isEnabled()){
 *       t.end();
 *     }   
 *   }
 * }
 * </pre>
 * 
 * @author Yanick Duchesne
 */
public class PerfAnalyzer implements MBeanFactory {
  private static final PerfAnalyzer _perf      = new PerfAnalyzer();
  private boolean                   _isEnabled;
  private Map<String, Topic>        _avgs = new ConcurrentHashMap<String, Topic>();

  /**
   * Constructor for PerfAnalyer.
   */
  private PerfAnalyzer() {
    super();
    _isEnabled = new PropUtil().addProperties(System.getProperties())
      .getBooleanProperty(Consts.STATS_ENABLED, false);
  }

  /**
   * @return <code>true</code> if average duration calculation is turned on.
   */
  public boolean isEnabled() {
    return _isEnabled;
  }

  /**
   * Enables average duration calculation.
   */
  public synchronized void enable(){
    _isEnabled = true;
    Topic[] topics;
    synchronized (_avgs) {
      topics = (Topic[]) _avgs.values().toArray(new Topic[_avgs.size()]);
      for(int i = 0; i < topics.length; i++){
        topics[i].setEnabled(true);
      }
    }
  }

  /**
   * Disables average duration calculation.
   */  
  public synchronized void disable(){
    _isEnabled = false;
    Topic[] topics;
    synchronized (_avgs) {
      topics = (Topic[]) _avgs.values().toArray(new Topic[_avgs.size()]);
      for(int i = 0; i < topics.length; i++){
        topics[i].setEnabled(false);
      }
    }
  }  

  /**
   * @param name the name of the topic to return.
   * @return the {@link Topic} with the given name - a new topic is created with
   * the given name if it does not already exist.
   */
  public Topic getTopic(String name) {
    Topic t = (Topic) _avgs.get(name);

    if (t == null) {
      synchronized (_avgs) {
        if ((t = (Topic) _avgs.get(name)) == null) {
          t = new Topic(name);
          _avgs.put(name, t);
          t.setEnabled(_isEnabled);
          return t;
        } else {
          return t;
        }
      }
    } else {
      return t;
    }
  }
  
  /**
   * @return the {@link Collection} of {@link Topic}s held by this instance.
   */
  public Collection<Topic> getTopics(){
    synchronized(_avgs){
      Collection<Topic> topics = new ArrayList<Topic>(_avgs.values());
      return topics;
    }
  }
  
  //////// MBeanFactory
  
  public MBeanContainer createMBean() throws Exception{
    ObjectName name = new ObjectName("sapia.ubik.rmi:type=PerfAnalyzer");
    return new MBeanContainer(name, new PerfAnalyzerMBean());
  }

  public static PerfAnalyzer getInstance() {
    return _perf;
  }
}
