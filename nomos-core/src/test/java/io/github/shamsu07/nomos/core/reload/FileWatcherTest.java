package io.github.shamsu07.nomos.core.reload;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileWatcherTest {

  @TempDir Path tempDir;

  private FileWatcher watcher;

  @BeforeEach
  void setup() throws IOException {
    watcher = new FileWatcher();
  }

  @AfterEach
  void cleanup() {
    if (watcher != null) {
      watcher.close();
    }
  }

  @Test
  void should_detectFileModification_when_fileChanged() throws Exception {
    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "initial content");

    CountDownLatch latch = new CountDownLatch(1);
    AtomicInteger callCount = new AtomicInteger(0);

    watcher.watch(
        testFile,
        () -> {
          callCount.incrementAndGet();
          latch.countDown();
        });

    // Wait for watcher to start
    Thread.sleep(100);

    // Modify file
    Files.writeString(testFile, "modified content");

    // Wait for callback
    boolean called = latch.await(5, TimeUnit.SECONDS);

    assertTrue(called, "Callback should be invoked");
    assertTrue(callCount.get() >= 1, "Callback should be called at least once");
  }

  @Test
  void should_throwException_when_fileDoesNotExist() {
    Path nonExistent = tempDir.resolve("missing.txt");
    assertThrows(IOException.class, () -> watcher.watch(nonExistent, () -> {}));
  }

  @Test
  void should_stopWatching_when_unwatchCalled() throws Exception {
    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "content");

    CountDownLatch latch = new CountDownLatch(1);
    watcher.watch(testFile, latch::countDown);

    Thread.sleep(100);

    // Unwatch before modification
    watcher.unwatch(testFile);

    // Modify file
    Files.writeString(testFile, "new content");

    // Callback should NOT be invoked
    boolean called = latch.await(1, TimeUnit.SECONDS);
    assertFalse(called, "Callback should not be invoked after unwatch");
  }

  @Test
  void should_watchMultipleFiles_when_multiplePaths() throws Exception {
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");
    Files.writeString(file1, "content1");
    Files.writeString(file2, "content2");

    CountDownLatch latch1 = new CountDownLatch(1);
    CountDownLatch latch2 = new CountDownLatch(1);

    watcher.watch(file1, latch1::countDown);
    watcher.watch(file2, latch2::countDown);

    Thread.sleep(100);

    Files.writeString(file1, "modified1");
    Files.writeString(file2, "modified2");

    assertTrue(latch1.await(5, TimeUnit.SECONDS), "File1 callback should be invoked");
    assertTrue(latch2.await(5, TimeUnit.SECONDS), "File2 callback should be invoked");
  }

  @Test
  void should_beRunning_when_watchingFiles() throws Exception {
    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "content");

    watcher.watch(testFile, () -> {});
    Thread.sleep(100);

    assertTrue(watcher.isRunning(), "Watcher should be running");
    assertEquals(1, watcher.getWatchedFileCount(), "Should have 1 watched file");
  }

  @Test
  void should_stopRunning_when_closed() throws Exception {
    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "content");

    watcher.watch(testFile, () -> {});
    Thread.sleep(100);

    watcher.close();
    Thread.sleep(100);

    assertFalse(watcher.isRunning(), "Watcher should not be running after close");
    assertEquals(0, watcher.getWatchedFileCount(), "Should have no watched files after close");
  }

  @Test
  void should_handleCallbackException_when_errorOccurs() throws Exception {
    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "content");

    CountDownLatch latch = new CountDownLatch(1);

    watcher.watch(
        testFile,
        () -> {
          latch.countDown();
          throw new RuntimeException("Test exception");
        });

    Thread.sleep(100);

    Files.writeString(testFile, "new content");

    // Callback should still be invoked despite exception
    boolean called = latch.await(5, TimeUnit.SECONDS);
    assertTrue(called, "Callback should be invoked even if it throws exception");

    // Watcher should still be running
    assertTrue(watcher.isRunning(), "Watcher should continue running after callback exception");
  }
}
