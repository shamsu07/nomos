package io.github.shamsu07.nomos.core.reload;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.engine.RuleEngine;
import io.github.shamsu07.nomos.core.expression.ExpressionEvaluator;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.loader.YAMLRuleLoader;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Rule engine with hot reload support.
 *
 * <p>Thread-safe. Reloads are atomic - engine continues using old rules if reload fails. Supports
 * automatic file watching for zero-downtime rule updates.
 */
public final class ReloadableRuleEngine implements AutoCloseable {

  private final AtomicReference<RuleEngine> engineRef;
  private final YAMLRuleLoader loader;
  private final FileWatcher fileWatcher;
  private final boolean stopOnFirstAppliedRule;

  private volatile Path rulesPath;
  private volatile String originalPath; // Keep original path for classpath resources
  private volatile ReloadListener reloadListener;

  public ReloadableRuleEngine(
      FunctionRegistry functionRegistry,
      ActionRegistry actionRegistry,
      boolean stopOnFirstAppliedRule)
      throws IOException {

    Objects.requireNonNull(functionRegistry, "FunctionRegistry cannot be null");
    Objects.requireNonNull(actionRegistry, "ActionRegistry cannot be null");

    ExpressionEvaluator evaluator = new ExpressionEvaluator(functionRegistry);
    this.loader = new YAMLRuleLoader(evaluator, functionRegistry, actionRegistry);
    this.engineRef = new AtomicReference<>(new RuleEngine(stopOnFirstAppliedRule));
    this.fileWatcher = new FileWatcher();
    this.stopOnFirstAppliedRule = stopOnFirstAppliedRule;
  }

  public ReloadableRuleEngine(FunctionRegistry functionRegistry, ActionRegistry actionRegistry)
      throws IOException {
    this(functionRegistry, actionRegistry, false);
  }

  /**
   * Load rules from path and enable hot reload
   *
   * @param path Path to YAML file
   * @param watchForChanges If true, automatically reload on file changes
   * @throws IOException if file cannot be read
   */
  public void loadRules(String path, boolean watchForChanges) throws IOException {
    Objects.requireNonNull(path, "Path cannot be null");

    // Store original path for classpath resources
    this.originalPath = path;

    // Resolve path (support classpath: prefix)
    Path resolvedPath = resolvePath(path);
    this.rulesPath = resolvedPath;

    // Initial load
    reload();

    // Setup file watching if requested
    if (watchForChanges) {
      if (resolvedPath == null) {
        // Classpath resource inside JAR - warn that hot reload is not possible
        System.err.println(
            "WARNING: Hot reload requested for '"
                + path
                + "' but classpath resources inside JAR files cannot be watched. "
                + "Hot reload will be disabled for this resource. "
                + "To enable hot reload, use a file system path instead.");
      } else if (Files.exists(resolvedPath)) {
        fileWatcher.watch(resolvedPath, this::reloadSafe);
      } else {
        System.err.println(
            "WARNING: Hot reload requested but resolved path does not exist: " + resolvedPath);
      }
    }
  }

  /**
   * Load rules without file watching
   *
   * @param path Path to YAML file
   */
  public void loadRules(String path) throws IOException {
    loadRules(path, false);
  }

  /**
   * Manually trigger reload from configured path
   *
   * @throws IOException if reload fails
   */
  public void reload() throws IOException {
    if (originalPath == null) {
      throw new IllegalStateException("No rules path configured. Call loadRules() first.");
    }

    long startTime = System.nanoTime();

    try (InputStream inputStream = openRulesInputStream()) {
      List<Rule> rules = loader.load(inputStream);

      // Create new engine with loaded rules
      RuleEngine newEngine = new RuleEngine(stopOnFirstAppliedRule);
      rules.forEach(newEngine::addRule);

      // Atomic swap
      RuleEngine oldEngine = engineRef.getAndSet(newEngine);

      long durationMs = (System.nanoTime() - startTime) / 1_000_000;

      // Notify listener (capture reference to avoid race condition)
      ReloadListener listener = this.reloadListener;
      if (listener != null) {
        listener.onReloadSuccess(rules.size(), durationMs);
      }

    } catch (Exception e) {
      long durationMs = (System.nanoTime() - startTime) / 1_000_000;

      // Notify listener (capture reference to avoid race condition)
      ReloadListener listener = this.reloadListener;
      if (listener != null) {
        listener.onReloadFailure(e, durationMs);
      }

      throw new IOException("Failed to reload rules: " + e.getMessage(), e);
    }
  }

  /** Reload rules, catching and logging errors (safe for file watcher callbacks) */
  private void reloadSafe() {
    try {
      reload();
    } catch (IOException e) {
      System.err.println("Failed to reload rules: " + e.getMessage());
    }
  }

  /**
   * Execute rules against facts (delegates to current engine)
   *
   * @param facts Input facts
   * @return Updated facts after rule execution
   */
  public Facts execute(Facts facts) {
    return engineRef.get().execute(facts);
  }

  /**
   * Execute rules with trace (delegates to current engine)
   *
   * @param facts Input facts
   * @return Execution result with trace
   */
  public RuleEngine.ExecutionResult executeWithTrace(Facts facts) {
    return engineRef.get().executeWithTrace(facts);
  }

  /**
   * Get current rules (snapshot)
   *
   * @return List of current rules
   */
  public List<Rule> getRules() {
    return engineRef.get().getRules();
  }

  /**
   * Set reload listener for notifications
   *
   * @param listener Reload listener
   */
  public void setReloadListener(ReloadListener listener) {
    this.reloadListener = listener;
  }

  /** Stop file watching and cleanup resources */
  @Override
  public void close() {
    fileWatcher.close();
  }

  /**
   * Validate the currently loaded rules. This method re-parses the rules from the configured path
   * and checks that all referenced functions and actions exist.
   *
   * <p>Use this for fail-fast validation at startup to catch configuration errors early.
   *
   * @return ValidationResult containing any errors found
   * @throws IOException if the rules file cannot be read
   */
  public ValidationResult validate() throws IOException {
    if (originalPath == null) {
      throw new IllegalStateException("No rules path configured. Call loadRules() first.");
    }

    List<String> errors = new ArrayList<>();

    try (InputStream inputStream = openRulesInputStream()) {
      // Attempt to load and parse all rules - this validates functions and actions exist
      loader.load(inputStream);
    } catch (Exception e) {
      // Collect the error message
      errors.add(e.getMessage());
      Throwable cause = e.getCause();
      while (cause != null) {
        errors.add("  Caused by: " + cause.getMessage());
        cause = cause.getCause();
      }
    }

    return new ValidationResult(errors);
  }

  /** Result of rule validation. */
  public static final class ValidationResult {
    private final List<String> errors;

    public ValidationResult(List<String> errors) {
      this.errors = List.copyOf(errors);
    }

    /**
     * Check if validation passed (no errors).
     *
     * @return true if validation passed
     */
    public boolean isValid() {
      return errors.isEmpty();
    }

    /**
     * Get list of validation errors.
     *
     * @return List of error messages (empty if valid)
     */
    public List<String> getErrors() {
      return errors;
    }

    /**
     * Get error count.
     *
     * @return Number of errors found
     */
    public int getErrorCount() {
      return errors.size();
    }

    @Override
    public String toString() {
      if (errors.isEmpty()) {
        return "ValidationResult[valid=true]";
      }
      return String.format("ValidationResult[valid=false, errors=%d: %s]", errors.size(), errors);
    }
  }

  /**
   * Opens an input stream for the configured rules path. Handles both filesystem paths and
   * classpath resources.
   *
   * @return InputStream for the rules file
   * @throws IOException if the resource cannot be opened
   */
  private InputStream openRulesInputStream() throws IOException {
    // For classpath resources (JAR resources), use classloader
    if (originalPath.startsWith("classpath:")) {
      String resourcePath = originalPath.substring("classpath:".length());
      InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath);
      if (stream == null) {
        throw new IOException("Classpath resource not found: " + resourcePath);
      }
      return stream;
    }

    // For filesystem paths, use rulesPath
    if (rulesPath != null) {
      return Files.newInputStream(rulesPath);
    }

    throw new IOException("Cannot open rules stream for: " + originalPath);
  }

  /**
   * Resolves a path string to a filesystem Path. Returns null for classpath resources that cannot
   * be resolved to a filesystem path (e.g., resources inside JAR files).
   *
   * @param path Path string (may have classpath: prefix)
   * @return Resolved Path, or null if resource is inside a JAR
   * @throws IOException if the resource is not found
   */
  private Path resolvePath(String path) throws IOException {
    // Handle classpath: prefix
    if (path.startsWith("classpath:")) {
      String resourcePath = path.substring("classpath:".length());

      // Verify resource exists
      var url = getClass().getClassLoader().getResource(resourcePath);
      if (url == null) {
        throw new IOException("Classpath resource not found: " + resourcePath);
      }

      // Try to convert to filesystem path (works for file:// URLs, not for jar:// URLs)
      try {
        if ("file".equals(url.getProtocol())) {
          return Paths.get(url.toURI());
        }
        // For JAR resources, return null - hot reload not supported
        return null;
      } catch (Exception e) {
        // Cannot convert to Path (likely a JAR resource) - return null
        return null;
      }
    }

    // Regular file path
    Path filePath = Paths.get(path);
    if (!Files.exists(filePath)) {
      throw new IOException("File not found: " + path);
    }
    return filePath;
  }

  /** Listener for reload events */
  public interface ReloadListener {
    /**
     * Called when reload succeeds
     *
     * @param ruleCount Number of rules loaded
     * @param durationMs Reload duration in milliseconds
     */
    void onReloadSuccess(int ruleCount, long durationMs);

    /**
     * Called when reload fails
     *
     * @param error Error that occurred
     * @param durationMs Time taken before failure
     */
    void onReloadFailure(Exception error, long durationMs);
  }
}
