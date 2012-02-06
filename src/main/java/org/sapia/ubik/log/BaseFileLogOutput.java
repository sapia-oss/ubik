package org.sapia.ubik.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.util.Exceptions;

/**
 * A {@link LogOutput} implementation that outputs logging statements to a file.
 * This class supports archiving, so that log files are archived up until a given maximum number
 * of archived files. Each archived file is identified with a counter (starting from 1). When the 
 * maximum is reached, archiving restarts from 1. 
 * <p>
 * An instance of this class creates log file in the current directory by default (corresponding to the
 * <code>user.dir</code> system property) - this may be overridden.
 * <p>
 * The full log file name has the following format:
 * 
 * <pre>
 * {fileName}.log
 * </pre>
 * 
 * For archived files, the counter is added:
 * 
 * <pre>
 * {fileName}-{counter}.log
 * </pre>
 * 
 * The value for <code>{fileName}</code> must be provided in the {@link Config} instance passed to this
 * constructor of this class.
 * 
 * The current log file is kept until its size reaches a given maximum number of megabytes
 * (defaulting to 3). When the current log file reaches that size, it is archived using the
 * next file counter.
 * <p>
 * The behavior of an instance of this class can be altered through system properties, or programatically.
 * (see the javadoc further below for the configuration properties that are supported).
 * <p>
 * Lastly, {@link FileArchivingListener} instances can be registered with an instance of this
 * class: these are notified whenever the current log file is archived.
 * 
 * @author yduchesne
 *
 */
public class BaseFileLogOutput implements LogOutput {
  
  private static final int ONE_MEG            = 1024 * 1024;
  private static final int LOG_CHECK_INTERVAL = 100;  
  
  private Config           conf;
  private int              logCheckInterval  = LOG_CHECK_INTERVAL;
  private File             currentLogFile;    
  private AtomicInteger    logCounter        = new AtomicInteger();
  private AtomicInteger    fileCounter       = new AtomicInteger();
  private volatile boolean closed;
  private FileWriter       output;
  private List<FileArchivingListener> listeners = Collections.synchronizedList(new ArrayList<FileArchivingListener>());
  
  protected BaseFileLogOutput(Config config) {
    this.conf = config;
  }
  
  public void addFileArchivingListener(FileArchivingListener listener) {
    this.listeners.add(listener);
  }
  
  @Override
  public void log(String msg) {
    if(output == null || closed) {
      try {
        createOutput();
      } catch (FileNotFoundException e) {
        throw new IllegalStateException("Could not create log file", e);
      }
    }
    output.write(msg);
    if(logCounter.incrementAndGet() >= logCheckInterval) {
      try {
        rotate();
      } catch (FileNotFoundException e) {
        throw new IllegalStateException("Could not rotate log file", e);
      }
    }
  }
  
  @Override
  public void log(Throwable error) {
    if(output == null || closed) {
      try {
        createOutput();
      } catch (FileNotFoundException e) {
        throw new IllegalStateException("Could not create log file", e);
      }
    }
    output.write(error);
    if(logCounter.incrementAndGet() >= logCheckInterval) {
      try {
        rotate();
      } catch (FileNotFoundException e) {
        throw new IllegalStateException("Could not rotate log file", e);
      }
    }
  }
  
  @Override
  public synchronized void close() {
    if(output != null && !closed) {
      output.close();
    }
    closed = true;    
  }
  
  private synchronized void createOutput() throws FileNotFoundException {
    if(output == null || closed) {
      currentLogFile = new File(conf.getLogDirectory(), conf.getLogFileName() + ".log");
      PrintWriter stream = new PrintWriter(currentLogFile);
      StreamFileWriter writer = new StreamFileWriter(stream);
      output = writer;
      closed = false;
    } 
  }
  
  private synchronized void rotate() throws FileNotFoundException {
    if(currentLogFile.length() >= ONE_MEG * conf.getMaxFileSize()) {
      InMemoryFileWriter inMemory = new InMemoryFileWriter();
      FileWriter oldOutput = output;
      output = inMemory;
      oldOutput.close();
      
      if(fileCounter.incrementAndGet() > conf.getMaxArchive()) {
        fileCounter.set(1);
      }
      File backLogFile = new File(conf.getLogDirectory(), conf.getLogFileName() + "-" + fileCounter + ".log");
      if(backLogFile.exists()) { 
        backLogFile.delete(); 
      }
      currentLogFile.renameTo(backLogFile);
      currentLogFile = new File(conf.getLogDirectory(), conf.getLogFileName() + ".log");
      
      synchronized(inMemory) {
        PrintWriter stream = new PrintWriter(currentLogFile);
        FileWriter newOutput = new StreamFileWriter(stream);
        inMemory.flushTo(newOutput);
        output = newOutput;
      }
     
      synchronized (listeners) {
        for(FileArchivingListener listener : listeners) {
          listener.onNewRotatedFile(backLogFile, fileCounter.get()); 
        }
      }
      logCounter.set(0);
    }
  }
  
  // ==========================================================================
  // INNER CLASSES  
  
  public static class Config {
   
    public static final int    DEFAULT_MAX_ARCHIVE        = 10;
    public static final int    DEFAULT_LOG_CHECK_INTERVAL = 100;
    public static final int    DEFAULT_MAX_FILE_SIZE      = 3;
    public static final String DEFAULT_LOG_DIRECTORY      = System.getProperty("user.dir");
    
    private File   logDirectory = new File(DEFAULT_LOG_DIRECTORY);
    private String logFileName;
    private int    archive      = DEFAULT_MAX_ARCHIVE;
    private int    maxFileSize  = DEFAULT_MAX_FILE_SIZE;

    public File getLogDirectory() {
      return logDirectory;
    }
    public Config setLogDirectory(File logDirectory) {
      if(!logDirectory.exists()) {
        logDirectory.mkdirs();
      }
      if(!logDirectory.isDirectory()) {
        throw new IllegalArgumentException("File is not a directory: " + logDirectory.getAbsolutePath());
      }
      this.logDirectory = logDirectory;
      
      return this;
    }
    
    public String getLogFileName() {
      if(logFileName == null) {
        throw new IllegalStateException("Log file name not set");
      }
      return logFileName;
    }
    public Config setLogFileName(String logFileName) {
      this.logFileName = logFileName;
      return this;
    }
    
    public int getMaxArchive() {
      return archive;
    }
    public Config setMaxArchive(int archive) {
      this.archive = archive;
      return this;
    }
    public int getMaxFileSize() {
      return maxFileSize;
    }
    public Config setMaxFileSize(int maxFileSize) {
      this.maxFileSize = maxFileSize;
      return this;
    }

  }

  // --------------------------------------------------------------------------  
  
  /**
   * An instance of this class can be registered with a {@link BaseFileLogOutput} to be
   * notified upon a log file being archived.
   */
  public interface FileArchivingListener {
    
    /**
     * @param archivedFile the {@link File} that was archived.
     * @param fileCounter the file's corresponding counter.
     */
    public void onNewRotatedFile(File archivedFile, int fileCounter);
      
  }

  // --------------------------------------------------------------------------
  
  private interface FileWriter {
    
    public void write(String content);
    
    public void write(Throwable err);
    
    public void close();
    
  }
  
  // --------------------------------------------------------------------------
  
  class StreamFileWriter implements FileWriter {
    
    private volatile PrintWriter writer;
    
    StreamFileWriter(PrintWriter writer) {
      this.writer = writer;
    }
    
    public void write(String content) {
      writer.println(content);
      writer.flush();
    }
    
    @Override
    public void write(Throwable err) {
      err.printStackTrace(writer);
      writer.flush();
    }
    
    @Override
    public void close() {
      writer.flush();
      writer.close();
    }
  }

  // --------------------------------------------------------------------------
  
  class InMemoryFileWriter implements FileWriter {
    
    private List<String> lines = new ArrayList<String>();
    
    @Override
    public synchronized void write(String content) {
      lines.add(content);
    }
    
    @Override
    public synchronized void write(Throwable err) {
      lines.add(Exceptions.stackTraceToString(err));
    }
    
    public synchronized void flushTo(FileWriter other) {
      for(String line : lines) {
        other.write(line);
      }
    }
    
    @Override
    public synchronized void close() {
    }
  }
}
