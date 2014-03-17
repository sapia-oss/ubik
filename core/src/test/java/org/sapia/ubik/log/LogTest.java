package org.sapia.ubik.log;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.Test;
import org.sapia.ubik.log.Log.Level;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.util.Conf;

public class LogTest {

  @After
  public void tearDown() throws Exception {
    Log.setDefaultLogLevel();
    Log.setDefaultLogOutput();
  }

  @Test
  public void testDetermineLogLevel() {
    for (Level lvl : Level.values()) {
      Properties props = new Properties();
      props.setProperty(Consts.LOG_LEVEL, lvl.name().toLowerCase());
      assertTrue("Could not determine log level", Log.determineLogLevel(new Conf().addProperties(props)));
      assertEquals(lvl, Log.getLevel());
    }
  }

  @Test
  public void testDetermineLogOutput() {
    Properties props = new Properties();
    props.setProperty(Consts.LOG_OUTPUT_CLASS, TestLogOutput.class.getName());
    assertTrue(Log.determineLogOutput(new Conf().addProperties(props)));
  }

  @Test
  public void testSetTrace() {
    Log.setTrace();
    assertEquals(Level.TRACE, Log.getLevel());
    assertTrue(Log.isLoggable(Level.TRACE));
    assertTrue(Log.isLoggable(Level.DEBUG));
    assertTrue(Log.isLoggable(Level.INFO));
    assertTrue(Log.isLoggable(Level.WARNING));
    assertTrue(Log.isLoggable(Level.ERROR));
    assertTrue(Log.isLoggable(Level.OFF));
    assertTrue(Log.isLoggable(Level.REPORT));
  }

  @Test
  public void testSetDebug() {
    Log.setDebug();
    assertFalse(Log.isLoggable(Level.TRACE));
    assertTrue(Log.isLoggable(Level.DEBUG));
    assertTrue(Log.isLoggable(Level.INFO));
    assertTrue(Log.isLoggable(Level.WARNING));
    assertTrue(Log.isLoggable(Level.ERROR));
    assertTrue(Log.isLoggable(Level.OFF));
    assertTrue(Log.isLoggable(Level.REPORT));
  }

  @Test
  public void testSetInfo() {
    Log.setInfo();
    assertFalse(Log.isLoggable(Level.TRACE));
    assertFalse(Log.isLoggable(Level.DEBUG));
    assertTrue(Log.isLoggable(Level.INFO));
    assertTrue(Log.isLoggable(Level.WARNING));
    assertTrue(Log.isLoggable(Level.ERROR));
    assertTrue(Log.isLoggable(Level.OFF));
    assertTrue(Log.isLoggable(Level.REPORT));
  }

  @Test
  public void testSetWarning() {
    Log.setWarning();
    assertFalse(Log.isLoggable(Level.TRACE));
    assertFalse(Log.isLoggable(Level.DEBUG));
    assertFalse(Log.isLoggable(Level.INFO));
    assertTrue(Log.isLoggable(Level.WARNING));
    assertTrue(Log.isLoggable(Level.ERROR));
    assertTrue(Log.isLoggable(Level.OFF));
    assertTrue(Log.isLoggable(Level.REPORT));
  }

  @Test
  public void testSetError() {
    Log.setError();
    assertFalse(Log.isLoggable(Level.TRACE));
    assertFalse(Log.isLoggable(Level.DEBUG));
    assertFalse(Log.isLoggable(Level.INFO));
    assertFalse(Log.isLoggable(Level.WARNING));
    assertTrue(Log.isLoggable(Level.ERROR));
    assertTrue(Log.isLoggable(Level.OFF));
    assertTrue(Log.isLoggable(Level.REPORT));
  }

  @Test
  public void testTurnOff() {
    Log.turnOff();
    assertFalse(Log.isLoggable(Level.TRACE));
    assertFalse(Log.isLoggable(Level.DEBUG));
    assertFalse(Log.isLoggable(Level.INFO));
    assertFalse(Log.isLoggable(Level.WARNING));
    assertFalse(Log.isLoggable(Level.ERROR));
    assertTrue(Log.isLoggable(Level.OFF));
    assertTrue(Log.isLoggable(Level.REPORT));
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

  @Test
  public void testIsOff() {
    Log.turnOff();
    assertTrue(Log.isOff());
  }

}
