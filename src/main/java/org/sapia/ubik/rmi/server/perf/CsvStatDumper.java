package org.sapia.ubik.rmi.server.perf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.taskman.Task;
import org.sapia.ubik.taskman.TaskContext;
import org.sapia.ubik.taskman.TaskManager;

/**
 * An instance of this class may be used to output Ubik stats in CSV format.
 * It is specified a file to use for ouput.
 * <p>
 * This class implements the {@link Task} interface, which allows for an instance
 * of it to be added to Ubik's task manager.
 * <p>
 * Snippet:
 * <pre>
 * CsvStatDumper dumper = new CsvStatDumper(new File("stats.csv"));
 * TaskContext ctx = new TaskContext("DumpStats", 120000);
 * Hub.taskMan.addTask(ctx, dumper);
 * </pre>
 * 
 * 
 * @see Hub#taskMan
   @see TaskManager#addTask(TaskContext, Task)
 * 
 * @author yduchesne
 *
 */
public class CsvStatDumper implements Task{
  
  private PrintStream _stream;
  private boolean _headerWritten;
  private SimpleDateFormat _dateFormat;
  /**
   * @param f the {@link File} to use for the output.
   * @throws Exception if a problem occurs while creating this instance.
   */
  public CsvStatDumper(File f) throws Exception{
    _stream = new PrintStream(new FileOutputStream(f));
    _dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SS");
  }
  
  public void exec(TaskContext ctx) {
    List<Statistic> stats = Hub.statsCollector.getStats();
    if(!_headerWritten){
      writeHeaders(stats);
      _headerWritten = true;
    }    
    writeContent(stats);
  }
  
  private void writeContent(List<Statistic> stats){
    _stream.print(getDate());
    _stream.print(",");
    for(int i = 0; i < stats.size(); i++){
      Statistic stat = (Statistic)stats.get(i);
      if(stat.isEnabled()){
        _stream.print(stat.getStat());
        if(i < stats.size() - 1){
          _stream.print(",");
        }
      }
    }
    _stream.println();
    _stream.flush();
  }
  
  private void writeHeaders(List<Statistic> stats){
    _stream.print("Time,");
    for(int i = 0; i < stats.size(); i++){
      Statistic stat = (Statistic)stats.get(i);
      if(stat.isEnabled()){
        _stream.print(stat.getName());
        if(i < stats.size() - 1){
          _stream.print(",");
        }
      }
    }
    _stream.println();
    _stream.flush();
  }  
  
  private String getDate(){
    return _dateFormat.format(new Date());
  }

}
