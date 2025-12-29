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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class FileWatcher implements AutoCloseable {

  private static final long DEFAULT_DEBOUNCE_DELAY_MS = 100;

  private final WatchService watchService;
  private final Map<Path, Runnable> watchedFiles;
  private final Map<Path, ScheduledFuture<?>> pendingCallbacks;
  private final ExecutorService watchExecutor;
  private final ExecutorService callbackExecutor;
  private final ScheduledExecutorService debounceScheduler;
  private final AtomicBoolean running;
  private final long debounceDelayMs;

  // Track registered directories and their reference counts
  private final Map<Path, WatchKey> registeredDirectories;
  private final Map<Path, AtomicInteger> directoryRefCounts;

  // Listener for watcher state changes
  private volatile WatcherStateListener stateListener;

  public FileWatcher() throws IOException {
    this(DEFAULT_DEBOUNCE_DELAY_MS);
  }

  /**
   * Create a FileWatcher with custom debounce delay
   *
   * @param debounceDelayMs Debounce delay in milliseconds
   * @throws IOException if watch service cannot be created
   */
  public FileWatcher(long debounceDelayMs) throws IOException {
    if (debounceDelayMs < 0) {
      throw new IllegalArgumentException("Debounce delay must be non-negative");
    }
    this.watchService = FileSystems.getDefault().newWatchService();
    this.watchedFiles = new ConcurrentHashMap<>();
    this.pendingCallbacks = new ConcurrentHashMap<>();
    this.debounceDelayMs = debounceDelayMs;
    this.registeredDirectories = new ConcurrentHashMap<>();
    this.directoryRefCounts = new ConcurrentHashMap<>();
    this.watchExecutor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = new Thread(r, "nomos-file-watcher");
              t.setDaemon(true);
              return t;
            });
    this.callbackExecutor =
        Executors.newCachedThreadPool(
            r -> {
              Thread t = new Thread(r, "nomos-callback");
              t.setDaemon(true);
              return t;
            });
    this.debounceScheduler =
        Executors.newScheduledThreadPool(
            1,
            r -> {
              Thread t = new Thread(r, "nomos-debounce-scheduler");
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

    // Register directory with watch service only if not already registered
    registerDirectory(directory);

    watchedFiles.put(absolutePath, onModified);

    // Start watching if not already running
    if (running.compareAndSet(false, true)) {
      watchExecutor.submit(this::watchLoop);
    }
  }

  /**
   * Watch a directory and invoke callback on any file modification within it.
   *
   * <p>Unlike {@link #watch(Path, Runnable)} which watches a specific file, this method triggers
   * the callback when ANY file in the directory is modified, created, or deleted.
   *
   * @param directoryPath Path to directory
   * @param onModified Callback to invoke when any file in directory changes
   * @throws IOException if directory doesn't exist or can't be watched
   */
  public void watchDirectory(Path directoryPath, Runnable onModified) throws IOException {
    Objects.requireNonNull(directoryPath, "Directory path cannot be null");
    Objects.requireNonNull(onModified, "Callback cannot be null");

    if (!Files.exists(directoryPath)) {
      throw new IOException("Directory does not exist: " + directoryPath);
    }

    if (!Files.isDirectory(directoryPath)) {
      throw new IOException("Path is not a directory: " + directoryPath);
    }

    Path absolutePath = directoryPath.toAbsolutePath();

    // Register directory with watch service
    registerDirectory(absolutePath);

    // Store with a special marker - we use the directory path itself as the key
    // The watchLoop will detect this and trigger for any file change in the
    // directory
    watchedFiles.put(absolutePath, onModified);

    // Start watching if not already running
    if (running.compareAndSet(false, true)) {
      watchExecutor.submit(this::watchLoop);
    }
  }

  /**
   * Register a directory with the watch service, tracking reference count
   *
   * @param directory Directory to register
   * @throws IOException if directory cannot be registered
   */
  private void registerDirectory(Path directory) throws IOException {
    // Check if directory is already registered
    WatchKey existingKey = registeredDirectories.get(directory);

    if (existingKey == null) {
      // First time registering this directory
      try {
        WatchKey key =
            directory.register(
                watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE);
        registeredDirectories.put(directory, key);
        directoryRefCounts.put(directory, new AtomicInteger(1));
      } catch (IOException e) {
        throw new IOException("Failed to register directory: " + directory, e);
      }
    } else {
      // Directory already registered, increment reference count
      AtomicInteger refCount = directoryRefCounts.get(directory);
      if (refCount != null) {
        refCount.incrementAndGet();
      }
    }
  }

  /**
   * Unregister a directory if no more files in it are being watched
   *
   * @param directory Directory to potentially unregister
   */
  private void unregisterDirectoryIfNeeded(Path directory) {
    AtomicInteger refCount = directoryRefCounts.get(directory);
    if (refCount == null) {
      return;
    }

    // Decrement reference count
    int newCount = refCount.decrementAndGet();

    // If no more files in this directory are being watched, unregister it
    if (newCount <= 0) {
      WatchKey key = registeredDirectories.remove(directory);
      directoryRefCounts.remove(directory);
      if (key != null) {
        key.cancel();
      }
    }
  }

  /**
   * remove file from watch list
   *
   * @param filePath Path to stop watching
   */
  public void unwatch(Path filePath) {
    Objects.requireNonNull(filePath, "File path cannot be null");
    Path absolutePath = filePath.toAbsolutePath();

    // Remove from watched files
    Runnable removed = watchedFiles.remove(absolutePath);

    // Only unregister directory if we were actually watching this file
    if (removed != null) {
      Path directory = absolutePath.getParent();
      if (directory != null) {
        unregisterDirectoryIfNeeded(directory);
      }
    }

    // Cancel any pending callback for this file
    ScheduledFuture<?> pendingCallback = pendingCallbacks.remove(absolutePath);
    if (pendingCallback != null && !pendingCallback.isDone()) {
      pendingCallback.cancel(false);
    }
  }

  /** Stop watching all files and shutdown */
  @Override
  public void close() {
    running.set(false);

    // Cancel all pending callbacks
    for (ScheduledFuture<?> future : pendingCallbacks.values()) {
      if (!future.isDone()) {
        future.cancel(false);
      }
    }
    pendingCallbacks.clear();

    // Cancel all watch keys
    for (WatchKey key : registeredDirectories.values()) {
      key.cancel();
    }
    registeredDirectories.clear();
    directoryRefCounts.clear();

    // Shutdown executors
    debounceScheduler.shutdown();
    watchExecutor.shutdown();
    callbackExecutor.shutdown();

    try {
      watchService.close();
    } catch (IOException e) {
      // Ignore close errors
    }
    watchedFiles.clear();
  }

  /**
   * Schedule a debounced callback for a path (file or directory).
   *
   * @param path The path key for debouncing
   * @param callback The callback to execute
   */
  private void scheduleCallback(Path path, Runnable callback) {
    // Debounce: cancel any pending callback for this path
    ScheduledFuture<?> existingFuture = pendingCallbacks.get(path);
    if (existingFuture != null && !existingFuture.isDone()) {
      existingFuture.cancel(false);
    }

    // Schedule new callback after debounce delay
    ScheduledFuture<?> future =
        debounceScheduler.schedule(
            () -> {
              // Remove from pending map
              pendingCallbacks.remove(path);
              // Execute callback in callback executor
              callbackExecutor.submit(
                  () -> {
                    try {
                      callback.run();
                    } catch (Exception e) {
                      // Log but don't crash watcher
                      System.err.println("Error in file watch callback: " + e.getMessage());
                    }
                  });
            },
            debounceDelayMs,
            TimeUnit.MILLISECONDS);

    pendingCallbacks.put(path, future);
  }

  private void watchLoop() {
    String exitReason = null;
    Throwable exitCause = null;
    boolean gracefulExit = false;

    try {
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

            // Get directory and full file path
            Path directory = (Path) key.watchable();
            Path absoluteDirectory = directory.toAbsolutePath();
            Path fullPath = directory.resolve(filename).toAbsolutePath();

            // Check for file-level callback first
            Runnable fileCallback = watchedFiles.get(fullPath);
            // Check for directory-level callback (for watchDirectory)
            Runnable directoryCallback = watchedFiles.get(absoluteDirectory);

            // Schedule file-level callback if present
            if (fileCallback != null) {
              scheduleCallback(fullPath, fileCallback);
            }

            // Schedule directory-level callback if present (and different from file
            // callback)
            if (directoryCallback != null && directoryCallback != fileCallback) {
              scheduleCallback(absoluteDirectory, directoryCallback);
            }
          }

          // Reset key to receive further events
          boolean valid = key.reset();
          if (!valid) {
            Path watchedDir = (Path) key.watchable();
            exitReason =
                "Watch key became invalid for directory: "
                    + watchedDir
                    + ". The directory may have been deleted or become inaccessible.";
            break;
          }

        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          // Check if this was a graceful shutdown
          if (!running.get()) {
            gracefulExit = true;
          } else {
            exitReason = "File watcher thread was interrupted";
            exitCause = e;
          }
          break;
        } catch (ClosedWatchServiceException e) {
          // This is expected when close() is called
          gracefulExit = !running.get();
          if (!gracefulExit) {
            exitReason = "Watch service was closed unexpectedly";
            exitCause = e;
          }
          break;
        }
      }
    } finally {
      // Always update running state when loop exits
      running.set(false);

      // Notify listener if exit was unexpected
      if (!gracefulExit && exitReason != null) {
        System.err.println("FileWatcher stopped: " + exitReason);
        WatcherStateListener listener = stateListener;
        if (listener != null) {
          try {
            listener.onWatcherStopped(exitReason, exitCause);
          } catch (Exception e) {
            System.err.println("Error in watcher state listener: " + e.getMessage());
          }
        }
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

  /** Get the debounce delay in milliseconds */
  public long getDebounceDelayMs() {
    return debounceDelayMs;
  }

  /** Get count of registered directories (for testing/debugging) */
  public int getRegisteredDirectoryCount() {
    return registeredDirectories.size();
  }

  /**
   * Set a listener to be notified of watcher state changes.
   *
   * @param listener Listener to notify, or null to remove
   */
  public void setStateListener(WatcherStateListener listener) {
    this.stateListener = listener;
  }

  /**
   * Get the current state listener.
   *
   * @return Current listener, or null if none set
   */
  public WatcherStateListener getStateListener() {
    return stateListener;
  }

  /** Listener interface for watcher state changes. */
  public interface WatcherStateListener {
    /**
     * Called when the watcher stops unexpectedly.
     *
     * @param reason Description of why the watcher stopped
     * @param cause Optional exception that caused the stop, may be null
     */
    void onWatcherStopped(String reason, Throwable cause);
  }
}
