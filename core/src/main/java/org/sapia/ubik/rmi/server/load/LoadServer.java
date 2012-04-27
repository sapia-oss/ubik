package org.sapia.ubik.rmi.server.load;

import java.io.File;
import java.util.Properties;

import javax.naming.Context;

import org.sapia.ubik.rmi.naming.remote.JNDIConsts;
import org.sapia.ubik.rmi.naming.remote.JNDIContextBuilder;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.nio.tcp.NioServerExporter;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexServerExporter;
import org.sapia.ubik.rmi.server.transport.socket.SocketServerExporter;
import org.sapia.ubik.util.PropertiesUtil;
import org.sapia.ubik.util.cli.Cmd;

/**
 * Binds a {@link LoadService} instance to the JNDI. Type <code>-help</code> at the command-line for help.
 * 
 * <p>
 * You can start a {@link LoadServer} with the <code>starter.sh</code> script that comes with Ubik's distribution:
 * 
 * <pre>
 * ./start.sh -c org.sapia.ubik.rmi.server.load.LoadServer [options]
 * </pre>
 * 
 * For help, type:
 * 
 * <pre>
 * ./start.sh -c org.sapia.ubik.rmi.server.load.LoadServer -help
 * </pre>
 * 
 * The following connects to the JNDI server on <code>localhost:1099</code> and loads properties from the specified file:
 * 
 * <pre>
 * ./start.sh -c org.sapia.ubik.rmi.server.load.LoadServer -h localhost -p 1099 -f /some/path/to/load-server.properties
 * </pre>
 *
 * See the documentation of the {@link LoadClient} class for more details about using the <code>starter.sh</code> script.
 * 
 * @author yduchesne
 *
 */
public class LoadServer {

	public static void main(String[] args) throws Exception {
		
		Cmd cmd = Cmd.fromArgs(args);
		
		if (cmd.hasSwitch("h")) {
			System.out.println("-s: The fully qualified name of the LoadService interface implementation");
			System.out.println("    to bind to the JNDI. Defaults to " + WorkLoadService.class.getName() + ".");
			System.out.println("-h: The host of the JNDI server to connect to. Defaults to localhost.");
			System.out.println("-p: The port of the JNDI server to connect to. Defaults to " + JNDIConsts.DEFAULT_PORT);
			System.out.println("-f: The path to the Ubik properties file to load (OPTIONAL)");
			System.out.println("-t: The type of transport to use. Value must be 'nio', 'standard' or 'mplex',");
			System.out.println("    without the quotes. Defaults to nio.");
			return;
		}
		
		LoadService impl = null;
	
		try {
  		impl = (LoadService) Class.forName(
  				cmd.hasSwitch("s") ? cmd.getOptWithValue("s").getTrimmedValueOrBlank() : NoopLoadService.class.getName()
  		).newInstance();
		} catch (Exception e) {
			System.out.println("Could not instantiate LoadService implementation, aborting");
			e.printStackTrace();
			return;
		}
		
		Properties props = new Properties();
		if (cmd.hasSwitch("f")) {
			File f = new File(cmd.getOptWithValue("f").getTrimmedValueOrBlank());
			PropertiesUtil.loadIntoPropertiesFrom(props, f);
		}
		PropertiesUtil.copy(props, System.getProperties());
		
		Context context = JNDIContextBuilder.newInstance().properties(props)
				.port(cmd.hasSwitch("p") ? cmd.getOptWithValue("p").getIntValue() : JNDIConsts.DEFAULT_PORT)
				.host(cmd.hasSwitch("h") ? cmd.getOptWithValue("h").getValue() : "localhost")
				.build();

		String transport = cmd.hasSwitch("t") ? cmd.getOptWithValue("t").getTrimmedValueOrBlank() : "nio";
		if (transport.equals("nio")) {
		  NioServerExporter exporter = new NioServerExporter();
  		context.bind(LoadService.JNDI_NAME, exporter.export(impl));
		} else if (transport.equals("standard")){
		  SocketServerExporter exporter = new SocketServerExporter();
		  context.bind(LoadService.JNDI_NAME, exporter.export(impl));
		} else if (transport.equals("mplex")) {
		  MultiplexServerExporter exporter = new MultiplexServerExporter();
      context.bind(LoadService.JNDI_NAME, exporter.export(impl));
		} else {
		  context.close();
		  Hub.shutdown();
		  System.out.println("Invalid transport: " + transport + ". Must be either nio, standard, or mplex");
		  return;
		}
    context.close();
		
    // registering shutdown hook
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        Hub.shutdown();
      }
    });

    // pausing until JVM termination
		while (true) {
			Thread.sleep(100000);
		}
		
  }
	
}
