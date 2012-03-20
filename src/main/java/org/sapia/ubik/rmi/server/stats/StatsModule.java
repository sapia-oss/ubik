package org.sapia.ubik.rmi.server.stats;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.taskman.TaskManager;
import org.sapia.ubik.util.Props;

/**
 * Clears all statistics at startup and shutdown. Also creates a task that dumps statistics to STDOUT
 * if the interval corresponding to {@link Consts#STATS_DUMP_INTERVAL} is specified and greater than
 * zero.
 * 
 * @author yduchesne
 *
 */
public class StatsModule implements Module {
  
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss:SSS");
  
  private StatsLogOutput statsLog;
  private long           lastDumpTime = System.currentTimeMillis();
  
  @Override
  public void init(ModuleContext context) {
    Stats.getInstance().clear();    
  }
  
  @Override
  public void start(ModuleContext context) {
    TaskManager taskManager = context.lookup(TaskManager.class);
    if(Stats.getInstance().isEnabled()){
      Props props = new Props().addProperties(System.getProperties());
      long dumpInterval = props.getLongProperty(Consts.STATS_DUMP_INTERVAL, 0);
      if(dumpInterval > 0){
        statsLog = new StatsLogOutput();
        dumpInterval = dumpInterval * 1000;
        Task task = new Task(){
          public void exec(TaskContext ctx) {
            long currentTime = System.currentTimeMillis();
            String startTime = dateFor(lastDumpTime);
            String endTime   = dateFor(currentTime);
            for(StatCapable stat : Stats.getInstance().getStatistics()){
              if(stat.isEnabled()){
                statsLog.log(format(stat, startTime, endTime));
              }
            }
            lastDumpTime = currentTime;
          }
        };
        taskManager.addTask(new TaskContext("DumpStats", dumpInterval), task);
      }
    }

  }
  
  private String format(StatCapable stat, String startTime, String endTime) {
    String statValue =  Double.toString(
                          BigDecimal
                            .valueOf(stat.getValue())
                            .setScale(4, BigDecimal.ROUND_HALF_UP)
                            .doubleValue()
                        );

    StringBuilder formatted = new StringBuilder();
    formatted.append(field(startTime))
      .append(",")
      .append(field(endTime))
      .append(",")
      .append(field(stat.getSource()))
      .append(",")
      .append(field(stat.getName()))
      .append(",")
      .append(field(stat.getDescription()))
      .append(",")
      .append(field(statValue));
    
    return formatted.toString();
  }
  
  private String field(String content) {
    return "\"" + content + "\""; 
  }
  
  private String dateFor(long millis) {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(millis);
    return DATE_FORMAT.format(cal.getTime());
  }
  
  @Override
  public void stop() {
  	if(statsLog != null) {
  		statsLog.close();
  	}
    Stats.getInstance().clear();
  }

}
