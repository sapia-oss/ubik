package org.sapia.ubik.rmi.server.perf;

import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An instance of this class internally keeps statistics and dumps their content.
 * 
 * @see #dumpStats(PrintStream)
 *  
 * @author yduchesne
 *
 */
public class StatsCollector {

  private static List _stats = Collections.synchronizedList(new ArrayList());
  private boolean _enabled = true;
  
  /**
   * "Enables" statistic output.
   *  
   * @param enabled the "enable" flag.
   */
  public void setEnabled(boolean enabled){
    _enabled = enabled;
    List stats = getStats();
    for(int i = 0; i < stats.size(); i++){
      Statistic stat = (Statistic)stats.get(i);
      stat.setEnabled(enabled);
    }
  }
  
  /**
   * This method adds the given statistic to this instance. The statistic
   * object is kept in a {@link SoftReference}
   * 
   * @param stat a {@link Statistic}
   */
  public StatsCollector addStat(Statistic stat){
    stat.setEnabled(_enabled);
    _stats.add(new SoftReference(stat));
    return this;
  }
  
  /**
   * @return the {@link List} of {@link Statistic}s held by this instance.
   */
  public List getStats(){
    List toReturn = new ArrayList(_stats.size());
    synchronized(_stats){
      for(int i = 0; i < _stats.size(); i++){
        SoftReference ref = (SoftReference)_stats.get(i);
        Statistic stat = (Statistic)ref.get();
        if(stat == null){
          _stats.remove(i--);
        }
        else{
          toReturn.add(stat);
        }
      }
    }
    return toReturn;
  }
  
  public void dumpStats(PrintStream ps){
    ps.println("================= Ubik Stats Dump at " + new Date() + " =================");
    List stats = getStats();
    for(int i = 0; i < stats.size(); i++){
      Statistic stat = (Statistic)stats.get(i);
      if(stat.isEnabled()){
        dumpStat(ps, stat.getName(), stat.getStat());
      }
    }
  }
  
  public void dumpStat(PrintStream ps, String name, Object value){
    int i = name.lastIndexOf('.');
    if(i > 0){
      name = name.substring(i+1);
    }
    ps.print(name);
    ps.print(":");
    printSpace(ps, name);    
    ps.println(value);       
  }  
  
  public void dumpStat(PrintStream ps, String name, double value){
    int i = name.lastIndexOf('.');
    if(i > 0){
      name = name.substring(i+1);
    }
    ps.print(name);
    ps.print(":");
    printSpace(ps, name);    
    ps.println(value);    
  }  
  
  private void printSpace(PrintStream ps, String name){
    int space = 60 - name.length();
    for(int i = 0; i < space; i++){
      ps.print(' ');
    }
  }
 
}
