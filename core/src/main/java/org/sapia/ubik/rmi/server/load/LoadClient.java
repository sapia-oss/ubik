package org.sapia.ubik.rmi.server.load;

import java.io.File;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.Context;

import org.javasimon.Counter;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.sapia.ubik.rmi.naming.remote.JNDIConsts;
import org.sapia.ubik.rmi.naming.remote.JNDIContextBuilder;
import org.sapia.ubik.rmi.naming.remote.Lookup;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.stats.Stats;
import org.sapia.ubik.util.PropertiesUtil;
import org.sapia.ubik.util.cli.Cmd;
import org.sapia.ubik.util.cli.Cmd.OptionFilter;
import org.sapia.ubik.util.cli.Opt;

/**
 * Looks up a {@link LoadService} from the JNDI and calls it non-stop, according to user-provided parameters.
 * Type <code>-h</code> at the command-line to start.
 * <p>
 * You can start a {@link LoadClient} with the <code>starter.sh</code> script that comes with Ubik's distribution:
 * 
 * <pre>
 * ./starter.sh org.sapia.ubik.rmi.server.load.LoadClient [options]
 * </pre>
 * 
 * For help, type:
 * 
 * <pre>
 * ./starter.sh org.sapia.ubik.rmi.server.load.LoadClient -help
 * </pre>
 * 
 * Another example, starting a {@link LoadClient} that connects to the JNDI server on <code>localhost:1099</code>
 * and loads properties from the specified file:
 * 
 * <pre>
 * ./starter.sh org.sapia.ubik.rmi.server.load.LoadClient -h localhost -p 1099 -f /some/path/to/load-client.properties
 * </pre>
 * 
 * Notes:
 * <ul>
 *  <li>You can specify additional classpath information, through the <code>UBIK_CP</code> environment variable. The
 *  value of that variable is expected to hold the path to additional librairies, if relevant.
 *  <li>You can specify custom JVM options with the <code>UBIK_OPTS</code> environment variable.
 * </ul>
 * 
 * @author yduchesne
 *
 */
public class LoadClient {

	public static void main(String[] args) throws Exception {
	  
    Cmd cmd = Cmd.fromArgs(args).filter(new OptionFilter() {
      @Override
      public boolean accepts(Opt option) {
        return !option.getName().contains(".sh");
      }
    });
		
		if (cmd.hasSwitch("help")) {
			System.out.println("-t: The number of concurrent threads that should be spawned. Defaults to 1.");
			System.out.println("-r: The random pause offset between each call, for each thread.");
			System.out.println("    Interpreted in millis. Defaults to 50.");
			System.out.println("-h: The host of the JNDI server to connect to. Defaults to localhost.");
			System.out.println("-p: The port of the JNDI server to connect to. Defaults to " + JNDIConsts.DEFAULT_PORT);
			System.out.println("-f: The path to the Ubik properties file to load (OPTIONAL)");
			return;
		}
		
		int       numThreads = cmd.hasSwitch("t") ? cmd.getOptWithValue("t").getIntValue() : 3;
		final int pause      = cmd.hasSwitch("r") ? cmd.getOptWithValue("r").getIntValue() : 50;
		
		Properties props = new Properties(System.getProperties());
		if (cmd.hasSwitch("f")) {
			File f = new File(cmd.getOptWithValue("f").getTrimmedValueOrBlank());
			PropertiesUtil.loadIntoPropertiesFrom(props, f);
		}
		PropertiesUtil.copy(props, System.getProperties());		
		
		final Context context = JNDIContextBuilder.newInstance().properties(props)
				.port(cmd.hasSwitch("p") ? cmd.getOptWithValue("p").getIntValue() : JNDIConsts.DEFAULT_PORT)
				.host(cmd.hasSwitch("h") ? cmd.getOptWithValue("h").getValue() : "localhost")
				.build();
		
    final LoadService loadService = Lookup.with(context).name(LoadService.JNDI_NAME).ofClass(LoadService.class);
		
		
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		
		final Counter   callsPerSecond = Stats.createCounter(
		                                LoadClient.class, 
		                                "ClientRemoteMethodCalls", 
		                                "The number of remote method calls")
		                                ;
		final Stopwatch callDuration  = Stats.createStopwatch(
		                                LoadClient.class, 
		                                "ClientRemoteMethodCallDuration", 
		                                "Remote method call duration");
		
		for (int t = 0; t < numThreads; t++) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
  				
					while (true) {
					  Split split = callDuration.start();
						loadService.perform();
						split.stop();
						callsPerSecond.increase();
						try {
							Thread.sleep(1 + new Random().nextInt(pause));
						} catch (InterruptedException e) {
						}
					}
				}
			});
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		  @Override
		  public void run() {
		    try {
		      context.close();
		    } catch (Exception e) {
		      // noop
		    }
		    
		    Hub.shutdown();
		  }
		});
		
		while (true) {
			Thread.sleep(100000);
		}
		
  }
	
}
