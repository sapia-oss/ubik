package org.sapia.ubik.rmi.server.stats;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.javasimon.Counter;
import org.javasimon.Simon;
import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContext;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.taskman.TaskManager;
import org.sapia.ubik.util.Props;

/**
 * Clears all statistics at startup and shutdown. Also creates a task that dumps
 * statistics to STDOUT if the interval corresponding to
 * {@link Consts#STATS_DUMP_INTERVAL} is specified and greater than zero.
 * 
 * @author yduchesne
 * 
 */
public class StatsModule implements Module {

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/mm/dd hh:mm:ss:SSS");
  private static final double NANOS_IN_MILLI = 1000000;
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("############.############");

  private Category log = Log.createCategory(getClass());
  private StatsLogOutput statsLog;
  private long lastDumpTime = System.currentTimeMillis();

  @Override
  public void init(ModuleContext context) {
  }

  @Override
  public void start(ModuleContext context) {
    TaskManager taskManager = context.lookup(TaskManager.class);
    if (SimonManager.isEnabled()) {
      Props props = new Props().addProperties(System.getProperties());
      long dumpInterval = props.getLongProperty(Consts.STATS_DUMP_INTERVAL, 0);
      if (dumpInterval > 0) {
        log.info("Stats dump interval set to %s seconds. Stats will be collected", dumpInterval);
        statsLog = new StatsLogOutput();
        dumpInterval = TimeUnit.MILLISECONDS.convert(dumpInterval, TimeUnit.SECONDS);
        Task task = new Task() {
          public void exec(TaskContext ctx) {
            long currentTime = System.currentTimeMillis();
            String startTime = dateFor(lastDumpTime);
            String endTime = dateFor(currentTime);

            for (Simon stat : Stats.getStats()) {
              statsLog.log(format(stat, startTime, endTime));
              Stats.reset(stat);
            }

            lastDumpTime = currentTime;
          }
        };
        taskManager.addTask(new TaskContext("DumpStats", dumpInterval), task);
      } else {
        log.info("Stats dump interval set to %s seconds. Stats will NOT be collected", dumpInterval);
      }
    }

  }

  private String format(Simon stat, String startTime, String endTime) {
    String statValue = statValue(stat);

    StringBuilder formatted = new StringBuilder();
    formatted.append(field(startTime)).append(",").append(field(endTime)).append(",").append(field(stat.getName())).append(",")
        .append(field(Stats.getDescription(stat))).append(",").append(field(statValue));

    return formatted.toString();
  }

  private String statValue(Simon stat) {
    if (stat instanceof Counter) {
      return Long.toString(((Counter) stat).getCounter());
    } else if (stat instanceof Stopwatch) {
      return DECIMAL_FORMAT.format(((Stopwatch) stat).getMean() / NANOS_IN_MILLI);
    } else {
      throw new IllegalStateException("Expected Counter or Stopwatch instance, got: " + stat);
    }
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
    if (statsLog != null) {
      statsLog.close();
    }
  }

}
