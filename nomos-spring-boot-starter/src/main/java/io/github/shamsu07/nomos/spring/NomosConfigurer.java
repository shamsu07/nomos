package io.github.shamsu07.nomos.spring;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;

/**
 * Callback interface for customizing Nomos function and action registries.
 *
 * <p>Implement this interface as a Spring bean to register custom functions and actions.
 */
@FunctionalInterface
public interface NomosConfigurer {

  /**
   * Configure function and action registries before rule loading.
   *
   * @param functionRegistry Function registry to register custom functions
   * @param actionRegistry Action registry to register custom actions
   */
  void configure(FunctionRegistry functionRegistry, ActionRegistry actionRegistry);
}
