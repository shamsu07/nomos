package io.github.shamsu07.nomos.core.source;

import io.github.shamsu07.nomos.core.rule.Rule;
import java.io.IOException;
import java.util.List;

/**
 * Source of rules that can be loaded and watched for changes.
 *
 * <p>Implementations provide different rule storage backends (filesystem, database, remote API).
 */
public interface RuleSource extends AutoCloseable {

  /**
   * Load all rules from this source
   *
   * @return List of rules
   * @throws IOException if loading fails
   */
  List<Rule> loadRules() throws IOException;

  /**
   * Enable hot reload by invoking callback when rules change
   *
   * @param reloadCallback Callback to invoke on changes
   * @throws IOException if hot reload setup fails
   */
  void enableHotReload(Runnable reloadCallback) throws IOException;
}
