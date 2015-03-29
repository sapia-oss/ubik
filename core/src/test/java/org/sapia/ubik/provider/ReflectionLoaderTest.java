package org.sapia.ubik.provider;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReflectionLoaderTest {

  @Test
  public void testLoad() {
    ReflectionLoader loader = new ReflectionLoader();
    LoaderTest testInstance = loader.load(LoaderTest.class, "default");
    assertTrue("Expected instance of LoaderTestImpl", testInstance instanceof LoaderTestImpl);
 }

}
