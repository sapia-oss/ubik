package org.sapia.ubik.mcast.avis;

import java.io.File;

import org.avis.logging.Log;
import org.avis.router.Router;
import org.avis.router.RouterOptions;
import org.sapia.ubik.util.Files;

public class TestRouterSetup {
  
  private Router router;
  private File   directory;
  
  public void setUp() throws Exception {
    Log.setApplicationName ("Avis");
    Log.enableLogging (Log.TRACE, false);
    Log.enableLogging (Log.DIAGNOSTIC, false);
    System.setProperty("avis.router.version", "1.2.2");
    RouterOptions config = new RouterOptions();
    File tmp = new File(System.getProperty("java.io.tmpdir"));
    directory = new File(tmp, "test-avis-router");
    directory.mkdirs();
    config.setRelativeDirectory(directory);
    router = new Router(config);  
    
  }
  
  public void tearDown() throws Exception {
    router.close();
    Files.visitDepthFirst(directory, Files.createFileDeletionVisitor());
  }
  

}
