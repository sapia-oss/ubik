package org.sapia.ubik.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.rmi.server.stub.Stub;

public class TestUtils {

  private TestUtils() {
    super();
  }

  public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
    Object o = ois.readObject();

    return o;
  }

  public static byte[] serialize(Object o) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream ous = new ObjectOutputStream(bos);
    ous.writeObject(o);
    ous.flush();
    ous.close();

    return bos.toByteArray();
  }

  public static Class<?>[] getInterfacesFor(Class<?> clazz) {
    HashSet<Class<?>> set = new HashSet<Class<?>>();
    Class<?> current = clazz;
    appendInterfaces(current, set);
    set.add(Stub.class);

    return (Class[]) set.toArray(new Class[set.size()]);
  }

  private static void appendInterfaces(Class<?> current, Set<Class<?>> interfaces) {
    Class<?>[] ifs = current.getInterfaces();

    for (int i = 0; i < ifs.length; i++) {
      appendInterfaces(ifs[i], interfaces);
      interfaces.add(ifs[i]);
    }

    current = current.getSuperclass();

    if (current != null) {
      appendInterfaces(current, interfaces);
    }
  }
}
