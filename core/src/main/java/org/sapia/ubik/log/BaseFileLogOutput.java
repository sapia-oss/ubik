package org.sapia.ubik.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Exceptions;
import org.sapia.ubik.util.Strings;

/**
 * A {@link LogOutput} implementation that outputs logging statements to a file.
 * This class supports archiving, so that log files are archived up until a
 * given maximum number of archived files. Each archived file is identified with
 * a counter (starting from 1). When the maximum is reached, archiving restarts
 * from 1.
 * <p>
 * An instance of this class creates log file in the current directory by
 * default (corresponding to the <code>user.dir</code> system property) - this
 * may be overridden.
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
 * The value for <code>{fileName}</code> must be provided in the {@link Config}
 * instance passed to this constructor of this class.
 * 
 * The current log file is kept until its size reaches a given maximum number of
 * megabytes (defaulting to 3). When the current log file reaches that size, it
 * is archived using the next file counter.
 * <p>
 * The behavior of an instance of this class can be altered through system
 * properties, or programatically. (see the javadoc further below for the
 * configuration properties that are supported).
 * <p>
 * Lastly, {@link FileArchivingListener} instances can be registered with an
 * instance of this class: these are notified whenever the current log file is
 * archived.
 * 
 * @author yduchesne
 * 
 */
public class BaseFileLogOutput implements LogOutput {

  private static final int ONE_MEG = 1024 * 1024;
  private static final int LOG_CHECK_INTERVAL = 100;

  private Category log = Log.createCategory(getClass());
  private Config conf;
  private int logCheckInterval = LOG_CHECK_INTERVAL;
  private File currentLogFile;
  private AtomicInteger logCounter = new AtomicInteger();
  private AtomicInteger fileCounter = new AtomicInteger();
  private volatile boolean closed;
  private FileWriter output;
  private List<FileArchivingListener> listeners = Collections.synchronizedList(new ArrayList<FileArchivingListener>());

  protected BaseFileLogOutput(Config config) {
    this.conf = config;
    log.info("Will perform stats logging using: %s", config);
  }

  public void addFileArchivingListener(FileArchivingListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void log(String msg) {
    if (output == null || closed) {
      createOutput();
    }
    output.write(msg);
    if (logCounter.incrementAndGet() >= logCheckInterval) {
      rotate();
    }
  }

  @Override
  public void log(Throwable error) {
    if (output == null || closed) {
      createOutput();
    }
    output.write(error);
    if (logCounter.incrementAndGet() >= logCheckInterval) {
      rotate();
    }
  }

  @Override
  public synchronized void close() {
    if (output != null && !closed) {
      output.close();
    }
    closed = true;
  }

  final void setLogCheckInterval(int logCheckInterval) {
    this.logCheckInterval = logCheckInterval;
  }

  final Config getConf() {
    return conf;
  }

  final int getFileCounter() {
    return fileCounter.get();
  }

  private synchronized void createOutput() {
    if (output == null || closed) {
      currentLogFile = new File(conf.getLogDirectory(), conf.getLogFileName() + ".log");
      output = createFileWriter(currentLogFile);
      closed = false;
    }
  }

  private synchronized void rotate() {
    if (isMaxSizeReached(currentLogFile)) {
      InMemoryFileWriter inMemory = new InMemoryFileWriter();
      FileWriter oldOutput = output;
      output = inMemory;
      oldOutput.close();

      if (fileCounter.incrementAndGet() > conf.getMaxArchive()) {
        fileCounter.set(1);
      }

      File backLogFile = new File(conf.getLogDirectory(), conf.getLogFileName() + "-" + fileCounter + ".log");
      deleteIfExists(backLogFile);
      rename(currentLogFile, backLogFile);

      currentLogFile = new File(conf.getLogDirectory(), conf.getLogFileName() + ".log");

      synchronized (inMemory) {
        FileWriter newOutput = createFileWriter(currentLogFile);
        inMemory.flushTo(newOutput);
        output = newOutput;
      }

      synchronized (listeners) {
        for (FileArchivingListener listener : listeners) {
          listener.onNewRotatedFile(backLogFile, fileCounter.get());
        }
      }
      logCounter.set(0);
    }
  }

  protected FileWriter createFileWriter(File target) {
    try {
      PrintWriter stream = new PrintWriter(target);
      return new StreamFileWriter(stream);
    } catch (FileNotFoundException e) {
      System.out.println("Could not create log file, will log to DEV/NULL");
      e.printStackTrace();
      return new NullFileWriter();
    }
  }

  protected boolean isMaxSizeReached(File currentLogFile) {
    return currentLogFile.length() >= ONE_MEG * conf.getMaxFileSize();
  }

  protected void deleteIfExists(File file) {
    if (file.exists()) {
      file.delete();
    }
  }

  protected void rename(File toRename, File to) {
    toRename.renameTo(to);
  }

  // ==========================================================================
  // INNER CLASSES

  public static class Config {

    public static final int DEFAULT_MAX_ARCHIVE = 10;
    public static final int DEFAULT_LOG_CHECK_INTERVAL = 100;
    public static final int DEFAULT_MAX_FILE_SIZE = 3;
    public static final String DEFAULT_LOG_DIRECTORY = System.getProperty("user.dir");

    private File logDirectory = new File(DEFAULT_LOG_DIRECTORY);
    private String logFileName;
    private int archive = DEFAULT_MAX_ARCHIVE;
    private int maxFileSize = DEFAULT_MAX_FILE_SIZE;

    public File getLogDirectory() {
      return logDirectory;
    }

    public Config setLogDirectory(File logDirectory) {
      if (!logDirectory.exists()) {
        logDirectory.mkdirs();
      }
      if (!logDirectory.isDirectory()) {
        throw new IllegalArgumentException("File is not a directory: " + logDirectory.getAbsolutePath());
      }
      this.logDirectory = logDirectory;

      return this;
    }

    public String getLogFileName() {
      Assertions.illegalState(logFileName == null, "Log file name not set");
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

    @Override
    public String toString() {
      return Strings.toStringFor(this, "directory", logDirectory, "fileName", logFileName, "archive", archive, "maxFileSize", maxFileSize);
    }

  }

  // --------------------------------------------------------------------------

  /**
   * An instance of this class can be registered with a
   * {@link BaseFileLogOutput} to be notified upon a log file being archived.
   */
  public interface FileArchivingListener {

    /**
     * @param archivedFile
     *          the {@link File} that was archived.
     * @param fileCounter
     *          the file's corresponding counter.
     */
    public void onNewRotatedFile(File archivedFile, int fileCounter);

  }

  // --------------------------------------------------------------------------

  interface FileWriter {

    public void write(String content);

    public void write(Throwable err);

    public void close();

  }

  // --------------------------------------------------------------------------

  class NullFileWriter implements FileWriter {

    @Override
    public void write(String content) {
    }

    @Override
    public void write(Throwable err) {
    }

    @Override
    public void close() {
    }

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

    private static final int MAX_LINES = 1000;

    private List<String> lines = new ArrayList<String>();

    @Override
    public synchronized void write(String content) {
      if (lines.size() >= MAX_LINES) {
        lines.remove(0);
      }
      lines.add(content);
    }

    @Override
    public synchronized void write(Throwable err) {
      write(Exceptions.stackTraceToString(err));
    }

    public synchronized void flushTo(FileWriter other) {
      for (String line : lines) {
        other.write(line);
      }
      lines.clear();
    }

    @Override
    public synchronized void close() {
    }
  }
}
