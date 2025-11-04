package io.github.shamsu07.nomos.core.reload;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FileWatcher implements AutoCloseable {

  private final WatchService watchService;
  private final Map<Path, Runnable> watchedFiles;
  private final ExecutorService executor;
  private final AtomicBoolean running;

  public FileWatcher() throws IOException {
    this.watchService = FileSystems.getDefault().newWatchService();
    this.watchedFiles = new ConcurrentHashMap<>();
    this.executor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = new Thread(r, "nomos-file-watcher");
              t.setDaemon(true);
              return t;
            });
    this.running = new AtomicBoolean(false);
  }

  /**
   * Watch a file and invoke callback on modification
   *
   * @param filePath Path to file
   * @param onModified Callback to invoke when file changes
   * @throws IOException if file doesn't exist or can't be watched
   */
  public void watch(Path filePath, Runnable onModified) throws IOException {
    Objects.requireNonNull(filePath, "File path cannot be null");
    Objects.requireNonNull(onModified, "Callback cannot be null");

    if (!Files.exists(filePath)) {
      throw new IOException("File does not exist: " + filePath);
    }

    Path absolutePath = filePath.toAbsolutePath();
    Path directory = absolutePath.getParent();

    // Register directory with watch service
    directory.register(
        watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);

    watchedFiles.put(absolutePath, onModified);

    // Start watching if not already running
    if (running.compareAndSet(false, true)) {
      executor.submit(this::watchLoop);
    }
  }

  /**
   * remove file from watch list
   *
   * @param filePath Path to stop watching
   */
  public void unwatch(Path filePath) {
    Objects.requireNonNull(filePath, "File path cannot be null");
    watchedFiles.remove(filePath.toAbsolutePath());
  }

  /** Stop watching all files and shutdown */
  @Override
  public void close() {
    running.set(false);
    executor.shutdown();
    try {
      watchService.close();
    } catch (IOException e) {
      // Ignore close errors
    }
    watchedFiles.clear();
  }

  private void watchLoop() {
    while (running.get()) {
      try {
        WatchKey key = watchService.take();

        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent.Kind<?> kind = event.kind();

          // Ignore overflow events
          if (kind == StandardWatchEventKinds.OVERFLOW) {
            continue;
          }

          @SuppressWarnings("unchecked")
          WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
          Path filename = pathEvent.context();

          // Check if this file is being watched
          Path directory = (Path) key.watchable();
          Path fullPath = directory.resolve(filename).toAbsolutePath();

          Runnable callback = watchedFiles.get(fullPath);
          if (callback != null) {
            // Invoke callback in separate thread to avoid blocking watch loop
            executor.submit(
                () -> {
                  try {
                    callback.run();
                  } catch (Exception e) {
                    // Log but don't crash watcher
                    System.err.println("Error in file watch callback: " + e.getMessage());
                  }
                });
          }
        }

        // Reset key to receive further events
        boolean valid = key.reset();
        if (!valid) {
          break;
        }

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (ClosedWatchServiceException e) {
        break;
      }
    }
  }

  /** Check if watcher is running */
  public boolean isRunning() {
    return running.get();
  }

  /** Get count of watched files */
  public int getWatchedFileCount() {
    return watchedFiles.size();
  }
}
