package org.sapia.ubik.log;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.PropUtil;

public class CategoryTest {

  private Category cat;
  private LogOutput output;

  @Before
  public void setUp() {
    PropUtil.clearUbikSystemProperties();
    // making sure components are shutdown
    Hub.shutdown();
    output = mock(LogOutput.class);
    cat = Log.createCategory(getClass());
    Log.setLogOutput(output);
  }

  @After
  public void tearDown() throws Exception {
    PropUtil.clearUbikSystemProperties();
    Log.setDefaultLogLevel();
    Log.setDefaultLogOutput();
  }

  @Test
  public void testTrace() {
    Log.setTrace();

    cat.trace("test");
    cat.trace("test", new Object[] {});

    cat.debug("test");
    cat.debug("test", new Object[] {});

    cat.info("test");
    cat.info("test", new Object[] {});

    cat.warning("test");
    cat.warning("test", new Object[] {});

    cat.error("test");
    cat.error("test", new Object[] {});

    cat.report("test");
    cat.report("test", new Object[] {});

    verify(output, times(12)).log(anyString());
  }

  @Test
  public void testDebug() {
    Log.setDebug();

    cat.trace("test");
    cat.trace("test", new Object[] {});

    cat.debug("test");
    cat.debug("test", new Object[] {});

    cat.info("test");
    cat.info("test", new Object[] {});

    cat.warning("test");
    cat.warning("test", new Object[] {});

    cat.error("test");
    cat.error("test", new Object[] {});

    cat.report("test");
    cat.report("test", new Object[] {});

    verify(output, times(10)).log(anyString());
  }

  @Test
  public void testInfo() {
    Log.setInfo();

    cat.trace("test");
    cat.trace("test", new Object[] {});

    cat.debug("test");
    cat.debug("test", new Object[] {});

    cat.info("test");
    cat.info("test", new Object[] {});

    cat.warning("test");
    cat.warning("test", new Object[] {});

    cat.error("test");
    cat.error("test", new Object[] {});

    cat.report("test");
    cat.report("test", new Object[] {});

    verify(output, times(8)).log(anyString());
  }

  @Test
  public void testWarning() {
    Log.setWarning();

    cat.trace("test");
    cat.trace("test", new Object[] {});

    cat.debug("test");
    cat.debug("test", new Object[] {});

    cat.info("test");
    cat.info("test", new Object[] {});

    cat.warning("test");
    cat.warning("test", new Object[] {});

    cat.error("test");
    cat.error("test", new Object[] {});

    cat.report("test");
    cat.report("test", new Object[] {});

    verify(output, times(6)).log(anyString());
  }

  @Test
  public void testError() {
    Log.setError();

    cat.trace("test");
    cat.trace("test", new Object[] {});

    cat.debug("test");
    cat.debug("test", new Object[] {});

    cat.info("test");
    cat.info("test", new Object[] {});

    cat.warning("test");
    cat.warning("test", new Object[] {});

    cat.error("test");
    cat.error("test", new Object[] {});

    cat.report("test");
    cat.report("test", new Object[] {});

    verify(output, times(4)).log(anyString());
  }

  @Test
  public void testOff() {
    Log.turnOff();

    cat.trace("test");
    cat.trace("test", new Object[] {});

    cat.debug("test");
    cat.debug("test", new Object[] {});

    cat.info("test");
    cat.info("test", new Object[] {});

    cat.warning("test");
    cat.warning("test", new Object[] {});

    cat.error("test");
    cat.error("test", new Object[] {});

    cat.report("test");
    cat.report("test", new Object[] {});

    verify(output, times(2)).log(anyString());
  }

  @Test
  public void testIsTrace() {
    Log.setTrace();
    assertTrue(Log.isTrace());
  }

  @Test
  public void testIsDebug() {
    Log.setDebug();
    assertTrue(Log.isDebug());
  }

  @Test
  public void testIsInfo() {
    Log.setInfo();
    assertTrue(Log.isInfo());
  }

  @Test
  public void testIsWarning() {
    Log.setWarning();
    assertTrue(Log.isWarning());
  }

  @Test
  public void testIsError() {
    Log.setError();
    assertTrue(Log.isError());
  }

}
