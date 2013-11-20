package org.sapia.ubik.log;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.ubik.log.BaseFileLogOutput.FileArchivingListener;
import org.sapia.ubik.log.BaseFileLogOutput.FileWriter;

public class BaseFileLogOutputTest {

  private TestFileLogOutput output;
  private FileWriter writer;

  @Before
  public void setUp() throws Exception {
    output = new TestFileLogOutput();
    output.setLogCheckInterval(0);
    writer = mock(FileWriter.class);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAddFileArchivingListenerCalled() {
    output.log("test");
    FileArchivingListener listener = mock(FileArchivingListener.class);
    output.addFileArchivingListener(listener);
    output.maxSizeReached = true;
    output.log("test");
    verify(listener, times(1)).onNewRotatedFile(any(File.class), anyInt());
  }

  @Test
  public void testFileCounter() {
    output.getConf().setMaxArchive(2);
    output.log("test");
    final AtomicInteger counter = new AtomicInteger();
    FileArchivingListener listener = mock(FileArchivingListener.class);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        counter.set((Integer) invocation.getArguments()[1]);
        return null;
      }
    }).when(listener).onNewRotatedFile(any(File.class), anyInt());

    output.addFileArchivingListener(listener);
    output.maxSizeReached = true;
    output.log("test");
    assertEquals(1, counter.get());
    output.log("test");
    assertEquals(2, counter.get());
    output.log("test");
    assertEquals(1, output.getFileCounter());
  }

  @Test
  public void testLogFirstTime() {
    output.log("test");
    assertEquals(1, output.creationCount);
    verify(writer, times(1)).write(anyString());
  }

  @Test
  public void testLogSecondTime() {
    output.log("test");
    assertEquals(1, output.creationCount);
    output.log("test");
    assertEquals(1, output.creationCount);
    verify(writer, times(2)).write(anyString());
  }

  @Test
  public void testLogMaxSizeReached() {
    output.log("test");
    assertEquals(1, output.creationCount);
    output.maxSizeReached = true;
    output.log("test");
    assertEquals(2, output.creationCount);
    assertEquals(1, output.deleteCount);
    assertEquals(1, output.renameCount);
  }

  @Test
  public void testLogThrowableFirstTime() {
    output.log(new Exception("test"));
    assertEquals(1, output.creationCount);
    verify(writer, times(1)).write(any(Exception.class));
  }

  @Test
  public void testLogThrowableSecondTime() {
    output.log(new Exception("test"));
    assertEquals(1, output.creationCount);
    verify(writer, times(1)).write(any(Exception.class));
    output.log(new Exception("test"));
    assertEquals(1, output.creationCount);
    verify(writer, times(2)).write(any(Exception.class));
  }

  @Test
  public void testLogThrowableMaxSizeReached() {
    output.log(new Exception("test"));
    assertEquals(1, output.creationCount);
    verify(writer, times(1)).write(any(Exception.class));
    output.maxSizeReached = true;
    output.log(new Exception("test"));
    assertEquals(2, output.creationCount);
    assertEquals(1, output.deleteCount);
    assertEquals(1, output.renameCount);
  }

  @Test
  public void testClose() {
    output.log("test");
    output.close();
    verify(writer, times(1)).close();
    assertEquals(1, output.closeCount);
    output.close();
    verify(writer, times(1)).close();
    assertEquals(2, output.closeCount);
  }

  class TestFileLogOutput extends BaseFileLogOutput {

    int creationCount, deleteCount, renameCount, closeCount;
    boolean maxSizeReached;

    public TestFileLogOutput() {
      super(new Config().setLogFileName("test"));
    }

    @Override
    protected FileWriter createFileWriter(File target) {
      creationCount++;
      return writer;
    }

    @Override
    protected boolean isMaxSizeReached(File currentLogFile) {
      return maxSizeReached;
    }

    @Override
    protected void deleteIfExists(File file) {
      deleteCount++;
    }

    @Override
    protected void rename(File toRename, File to) {
      renameCount++;
    }

    @Override
    public synchronized void close() {
      closeCount++;
      super.close();
    }

  }

}
