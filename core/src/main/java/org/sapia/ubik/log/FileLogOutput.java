package org.sapia.ubik.log;

import java.io.File;

import org.sapia.ubik.util.Props;

/**
 * This class inherits from {@link BaseFileLogOutput}: it specifies a default
 * log file name, which is set to "ubik". This means that the current log file
 * will be named as follows:
 * 
 * <pre>
 * ubik.log
 * </pre>
 * 
 * The configuration of this instance can be specified through system properties
 * - see the javadoc further below for more information.
 * 
 * @author yduchesne
 * 
 */
public class FileLogOutput extends BaseFileLogOutput {

  /**
   * Corresponds to the number of archived files that are kept (
   * <code>ubik.rmi.log.file.max-archive</code>). Defaults to 10.
   */
  public static final String PROP_MAX_ARCHIVE = "ubik.rmi.log.file.max-archive";
  /**
   * Corresponds to the log file name (<code>ubik.rmi.log.file.name</code>).
   * Defaults to <code>ubik</code>.
   */
  public static final String PROP_FILE_NAME = "ubik.rmi.log.file.name";
  /**
   * Corresponds to the maximum file size (in megabytes) before the current log
   * file is archived. (<code>ubik.rmi.log.file.size</code>). Defaults to 3
   * megabytes.
   */
  public static final String PROP_FILE_SIZE = "ubik.rmi.log.file.size";

  /**
   * Corresponds to the directory where log files are created. (
   * <code>ubik.rmi.log.file.dir</code>). Defaults to the <code>user.dir</code>
   * system property.
   */
  public static final String PROP_FILE_DIR = "ubik.rmi.log.file.dir";

  private static final Props PROPS = Props.getSystemProperties();

  public static final String DEFAULT_FILE_NAME = "ubik";

  public FileLogOutput() {
    super(new Config().setMaxArchive(PROPS.getIntProperty(PROP_MAX_ARCHIVE, Config.DEFAULT_MAX_ARCHIVE))
        .setLogDirectory(new File(PROPS.getProperty(PROP_FILE_DIR, Config.DEFAULT_LOG_DIRECTORY)))
        .setLogFileName(PROPS.getProperty(PROP_FILE_NAME, DEFAULT_FILE_NAME))
        .setMaxFileSize(PROPS.getIntProperty(PROP_FILE_SIZE, Config.DEFAULT_MAX_FILE_SIZE)));
  }

}
