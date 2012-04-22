package org.sapia.ubik.rmi.examples.load;

import java.io.File;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.naming.Context;
import javax.naming.NamingException;

import org.sapia.ubik.rmi.naming.remote.JNDIContextBuilder;
import org.sapia.ubik.rmi.naming.remote.JNDIConsts;
import org.sapia.ubik.rmi.naming.remote.Lookup;
import org.sapia.ubik.util.PropertiesUtil;
import org.sapia.ubik.util.cli.Cmd;

public class LoadClient {

	public static void main(String[] args) throws Exception {
		
		Cmd cmd = Cmd.fromArgs(args);
		
		if (cmd.hasSwitch("h")) {
			System.out.println("-t: The number of concurrent threads that should be spawned. Defaults to 3.");
			System.out.println("-s: The random pause offset between each call, for each thread.");
			System.out.println("    Interpreted in millis. Defaults to 50.");
			System.out.println("-h: The host of the JNDI server to connect to. Defaults to localhost.");
			System.out.println("-p: The port of the JNDI server to connect to. Defaults to " + JNDIConsts.DEFAULT_PORT);
			System.out.println("-f: The path to the Ubik properties file to load (OPTIONAL)");
			return;
		}
		
		int       numThreads = cmd.hasSwitch("t") ? cmd.getOptWithValue("t").getIntValue() : 3;
		final int pause      = cmd.hasSwitch("s") ? cmd.getOptWithValue("s").getIntValue() : 50;
		
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
		
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		
		final Timer timer = time. 
		
		for (int t = 0; t < numThreads; t++) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					
  				LoadService loadService = null;
  				try {
  					loadService = Lookup.with(context).forName(LoadService.class, LoadService.JNDI_NAME);
  				} catch (Exception e) {
  					System.out.println("LoadService could not be looked up, aborting.");
  					e.printStackTrace();
  					try {
  						context.close();
  					} catch (Exception e2) {
  					}
  					return;
  				}
  				
					while (true) {
						long start = System.currentTimeMillis();
						loadService.perform();
						try {
							//System.out.println(String.format("LoadClient count = %s, duration = %s ", calls.incrementAndGet(), (System.currentTimeMillis() - start)));
							//Thread.sleep(10);
							Thread.sleep(1 + new Random().nextInt(pause));
						} catch (InterruptedException e) {
						}
					}
				}
			});
		}
			  
		while (true) {
			Thread.sleep(100000);
		}
		
  }
	
}
