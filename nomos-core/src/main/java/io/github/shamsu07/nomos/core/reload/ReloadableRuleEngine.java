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

    // Resolve path (support classpath: prefix)
    Path resolvedPath = resolvePath(path);
    this.rulesPath = resolvedPath;

    // Initial load
    reload();

    // Setup file watching if requested
    if (watchForChanges && Files.exists(resolvedPath)) {
      fileWatcher.watch(resolvedPath, this::reloadSafe);
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
    if (rulesPath == null) {
      throw new IllegalStateException("No rules path configured. Call loadRules() first.");
    }

    long startTime = System.nanoTime();

    try (InputStream inputStream = Files.newInputStream(rulesPath)) {
      List<Rule> rules = loader.load(inputStream);

      // Create new engine with loaded rules
      RuleEngine newEngine = new RuleEngine(stopOnFirstAppliedRule);
      rules.forEach(newEngine::addRule);

      // Atomic swap
      RuleEngine oldEngine = engineRef.getAndSet(newEngine);

      long durationMs = (System.nanoTime() - startTime) / 1_000_000;

      // Notify listener
      if (reloadListener != null) {
        reloadListener.onReloadSuccess(rules.size(), durationMs);
      }

    } catch (Exception e) {
      long durationMs = (System.nanoTime() - startTime) / 1_000_000;

      if (reloadListener != null) {
        reloadListener.onReloadFailure(e, durationMs);
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

  private Path resolvePath(String path) throws IOException {
    // Handle classpath: prefix
    if (path.startsWith("classpath:")) {
      String resourcePath = path.substring("classpath:".length());

      // Try to find resource
      var url = getClass().getClassLoader().getResource(resourcePath);
      if (url == null) {
        throw new IOException("Classpath resource not found: " + resourcePath);
      }

      try {
        return Paths.get(url.toURI());
      } catch (Exception e) {
        throw new IOException("Cannot resolve classpath resource: " + resourcePath, e);
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
