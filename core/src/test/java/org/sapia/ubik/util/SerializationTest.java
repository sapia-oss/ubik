package org.sapia.ubik.util;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.Test;

public class SerializationTest {

	@Test
	public void testDeserializeByteArray() throws Exception {
		byte[] bytes = Serialization.serialize(new SerializableObj());
		assertTrue(Serialization.deserialize(bytes) instanceof SerializableObj);
	}

	@Test
	public void testDeserializeWithClassLoader() throws Exception {
		byte[] bytes = Serialization.serialize(new SerializableObj());
		assertTrue(Serialization.deserialize(bytes, Thread.currentThread().getContextClassLoader()) instanceof SerializableObj);
	}

	@Test
	public void testSerialize() throws Exception {
		Serialization.serialize(new SerializableObj());
	}

	public static class SerializableObj implements Serializable {
		
		private int value = 1;
		
	}
}
