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

  @Test
  void should_debounceMultipleRapidChanges_when_fileModifiedQuickly() throws Exception {
    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "initial content");

    AtomicInteger callCount = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(1);

    watcher.watch(
        testFile,
        () -> {
          callCount.incrementAndGet();
          latch.countDown();
        });

    Thread.sleep(100);

    // Make multiple rapid changes within the debounce window
    for (int i = 0; i < 5; i++) {
      Files.writeString(testFile, "content " + i);
      Thread.sleep(20); // Sleep less than debounce delay (100ms)
    }

    // Wait for debounced callback
    boolean called = latch.await(5, TimeUnit.SECONDS);

    assertTrue(called, "Callback should be invoked");
    // Due to debouncing, callback should be called only once (or very few times)
    // Allow for some variance due to timing
    assertTrue(
        callCount.get() <= 2,
        "Callback should be called at most 2 times due to debouncing, but was called "
            + callCount.get()
            + " times");
  }

  @Test
  void should_invokeCallbackMultipleTimes_when_changesAreSpacedOut() throws Exception {
    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "initial content");

    AtomicInteger callCount = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(2);

    watcher.watch(
        testFile,
        () -> {
          callCount.incrementAndGet();
          latch.countDown();
        });

    Thread.sleep(100);

    // Make changes spaced out beyond the debounce window
    Files.writeString(testFile, "content 1");
    Thread.sleep(200); // Wait longer than debounce delay

    Files.writeString(testFile, "content 2");
    Thread.sleep(200); // Wait longer than debounce delay

    // Wait for callbacks
    boolean called = latch.await(5, TimeUnit.SECONDS);

    assertTrue(called, "Both callbacks should be invoked");
    assertTrue(callCount.get() >= 2, "Callback should be called at least twice");
  }

  @Test
  void should_useCustomDebounceDelay_when_specified() throws Exception {
    watcher.close();
    watcher = new FileWatcher(50); // 50ms debounce delay

    assertEquals(50, watcher.getDebounceDelayMs(), "Debounce delay should be 50ms");

    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "initial content");

    AtomicInteger callCount = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(1);

    watcher.watch(
        testFile,
        () -> {
          callCount.incrementAndGet();
          latch.countDown();
        });

    Thread.sleep(100);

    // Make multiple rapid changes within the 50ms debounce window
    for (int i = 0; i < 5; i++) {
      Files.writeString(testFile, "content " + i);
      Thread.sleep(10); // Sleep less than 50ms debounce delay
    }

    // Wait for debounced callback
    boolean called = latch.await(5, TimeUnit.SECONDS);

    assertTrue(called, "Callback should be invoked");
    assertTrue(
        callCount.get() <= 2,
        "Callback should be called at most 2 times due to debouncing with 50ms delay");
  }

  @Test
  void should_useDefaultDebounceDelay_when_notSpecified() throws Exception {
    assertEquals(100, watcher.getDebounceDelayMs(), "Default debounce delay should be 100ms");
  }

  @Test
  void should_cancelPendingCallback_when_unwatchCalled() throws Exception {
    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "content");

    AtomicInteger callCount = new AtomicInteger(0);
    watcher.watch(testFile, callCount::incrementAndGet);

    Thread.sleep(100);

    // Modify file
    Files.writeString(testFile, "new content");

    // Immediately unwatch before debounce delay expires
    Thread.sleep(20); // Wait a bit but less than debounce delay
    watcher.unwatch(testFile);

    // Wait beyond debounce delay
    Thread.sleep(200);

    // Callback should not be invoked since we unwatched
    assertEquals(
        0,
        callCount.get(),
        "Callback should not be invoked after unwatch cancels pending callback");
  }

  @Test
  void should_cancelAllPendingCallbacks_when_closed() throws Exception {
    Path testFile1 = tempDir.resolve("file1.txt");
    Path testFile2 = tempDir.resolve("file2.txt");
    Files.writeString(testFile1, "content1");
    Files.writeString(testFile2, "content2");

    AtomicInteger callCount1 = new AtomicInteger(0);
    AtomicInteger callCount2 = new AtomicInteger(0);

    watcher.watch(testFile1, callCount1::incrementAndGet);
    watcher.watch(testFile2, callCount2::incrementAndGet);

    Thread.sleep(100);

    // Modify both files
    Files.writeString(testFile1, "modified1");
    Files.writeString(testFile2, "modified2");

    // Immediately close before debounce delay expires
    Thread.sleep(20);
    watcher.close();

    // Wait beyond debounce delay
    Thread.sleep(200);

    // Callbacks should not be invoked since watcher was closed
    assertEquals(
        0, callCount1.get(), "File1 callback should not be invoked after close cancels pending");
    assertEquals(
        0, callCount2.get(), "File2 callback should not be invoked after close cancels pending");
  }

  @Test
  void should_throwException_when_negativeDebounceDelay() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new FileWatcher(-1),
        "Should throw exception for negative debounce delay");
  }

  @Test
  void should_allowZeroDebounceDelay_when_noDebounceNeeded() throws Exception {
    watcher.close();
    watcher = new FileWatcher(0); // No debouncing

    Path testFile = tempDir.resolve("test.txt");
    Files.writeString(testFile, "initial content");

    AtomicInteger callCount = new AtomicInteger(0);
    CountDownLatch latch = new CountDownLatch(3);

    watcher.watch(
        testFile,
        () -> {
          callCount.incrementAndGet();
          latch.countDown();
        });

    Thread.sleep(100);

    // Make multiple rapid changes
    for (int i = 0; i < 3; i++) {
      Files.writeString(testFile, "content " + i);
      Thread.sleep(50);
    }

    // Wait for all callbacks
    boolean called = latch.await(5, TimeUnit.SECONDS);

    assertTrue(called, "All callbacks should be invoked");
    assertTrue(callCount.get() >= 3, "With zero debounce, all callbacks should be invoked");
  }

  @Test
  void should_registerDirectoryOnce_when_watchingMultipleFilesInSameDirectory() throws Exception {
    // Create multiple files in the same directory
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");
    Path file3 = tempDir.resolve("file3.txt");
    Files.writeString(file1, "content1");
    Files.writeString(file2, "content2");
    Files.writeString(file3, "content3");

    // Watch all three files
    watcher.watch(file1, () -> {});
    watcher.watch(file2, () -> {});
    watcher.watch(file3, () -> {});

    Thread.sleep(100);

    // Verify only one directory is registered
    assertEquals(1, watcher.getRegisteredDirectoryCount(), "Should register directory only once");
    assertEquals(3, watcher.getWatchedFileCount(), "Should have 3 watched files");
  }

  @Test
  void should_unregisterDirectory_when_allFilesUnwatched() throws Exception {
    // Create multiple files in the same directory
    Path file1 = tempDir.resolve("file1.txt");
    Path file2 = tempDir.resolve("file2.txt");
    Files.writeString(file1, "content1");
    Files.writeString(file2, "content2");

    // Watch both files
    watcher.watch(file1, () -> {});
    watcher.watch(file2, () -> {});

    Thread.sleep(100);

    // Verify directory is registered
    assertEquals(1, watcher.getRegisteredDirectoryCount(), "Should have 1 registered directory");

    // Unwatch first file
    watcher.unwatch(file1);
    assertEquals(1, watcher.getRegisteredDirectoryCount(), "Directory should still be registered");
    assertEquals(1, watcher.getWatchedFileCount(), "Should have 1 watched file");

    // Unwatch second file
    watcher.unwatch(file2);
    assertEquals(0, watcher.getRegisteredDirectoryCount(), "Directory should be unregistered");
    assertEquals(0, watcher.getWatchedFileCount(), "Should have no watched files");
  }

  @Test
  void should_handleMultipleDirectories_when_watchingFilesInDifferentDirectories()
      throws Exception {
    // Create files in different directories
    Path dir1 = tempDir.resolve("dir1");
    Path dir2 = tempDir.resolve("dir2");
    Files.createDirectory(dir1);
    Files.createDirectory(dir2);

    Path file1 = dir1.resolve("file1.txt");
    Path file2 = dir2.resolve("file2.txt");
    Files.writeString(file1, "content1");
    Files.writeString(file2, "content2");

    // Watch files in different directories
    watcher.watch(file1, () -> {});
    watcher.watch(file2, () -> {});

    Thread.sleep(100);

    // Verify both directories are registered
    assertEquals(2, watcher.getRegisteredDirectoryCount(), "Should register both directories");
    assertEquals(2, watcher.getWatchedFileCount(), "Should have 2 watched files");
  }
}
