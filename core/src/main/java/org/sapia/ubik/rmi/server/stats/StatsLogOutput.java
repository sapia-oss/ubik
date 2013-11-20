package org.sapia.ubik.rmi.server.stats;

import java.io.File;

import org.sapia.ubik.log.BaseFileLogOutput;
import org.sapia.ubik.util.Props;

/**
 * This class inherits from {@link BaseFileLogOutput}: it specifies a default
 * log file name, which is set to "ubik-stats". This means that the current log
 * file will be named as follows:
 * 
 * <pre>
 * ubik - stats.log
 * </pre>
 * 
 * The configuration of this instance can be specified through system properties
 * - see the javadoc further below for more information.
 * 
 * @author yduchesne
 * 
 */
public class StatsLogOutput extends BaseFileLogOutput {

  /**
   * Corresponds to the number of archived files that are kept (
   * <code>ubik.rmi.stats.log.file.max-archive</code>). Defaults to 10.
   */
  public static final String PROP_MAX_ARCHIVE = "ubik.rmi.stats.log.file.max-archive";
  /**
   * Corresponds to the log file name (<code>ubik.rmi.stats.log.file.name</code>
   * ). Defaults to <code>ubik</code>.
   */
  public static final String PROP_FILE_NAME = "ubik.rmi.stats.log.file.name";
  /**
   * Corresponds to the maximum file size (in megabytes) before the current log
   * file is archived. (<code>ubik.rmi.stats.log.file.size</code>). Defaults to
   * 3 megabytes.
   */
  public static final String PROP_FILE_SIZE = "ubik.rmi.stats.log.file.size";

  /**
   * Corresponds to the directory where log files are created. (
   * <code>ubik.rmi.stats.log.file.dir</code>). Defaults to the
   * <code>user.dir</code> system property.
   */
  public static final String PROP_FILE_DIR = "ubik.rmi.stats.log.file.dir";

  private static final Props PROPS = Props.getSystemProperties();

  public static final String DEFAULT_FILE_NAME = "ubik-stats";

  public StatsLogOutput() {
    super(new Config().setMaxArchive(PROPS.getIntProperty(PROP_MAX_ARCHIVE, Config.DEFAULT_MAX_ARCHIVE))
        .setLogDirectory(new File(PROPS.getProperty(PROP_FILE_DIR, Config.DEFAULT_LOG_DIRECTORY)))
        .setLogFileName(PROPS.getProperty(PROP_FILE_NAME, DEFAULT_FILE_NAME))
        .setMaxFileSize(PROPS.getIntProperty(PROP_FILE_SIZE, Config.DEFAULT_MAX_FILE_SIZE)));
  }

}
