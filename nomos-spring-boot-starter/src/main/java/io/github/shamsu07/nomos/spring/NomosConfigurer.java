package io.github.shamsu07.nomos.spring;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import org.springframework.core.Ordered;

/**
 * Callback interface for customizing Nomos function and action registries.
 *
 * <p>Implement this interface as a Spring bean to register custom functions and actions.
 *
 * <p>Multiple configurers are executed in order based on their {@link #getOrder()} value. Lower
 * values have higher priority. The default order is {@link Ordered#LOWEST_PRECEDENCE}.
 * Alternatively, you can use the {@link org.springframework.core.annotation.Order @Order}
 * annotation on your configurer bean.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Bean
 * public NomosConfigurer baseConfigurer() {
 *   return new NomosConfigurer() {
 *     @Override
 *     public void configure(FunctionRegistry functionRegistry, ActionRegistry actionRegistry) {
 *       // Register base functions/actions
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *       return 0; // Execute first
 *     }
 *   };
 * }
 *
 * @Bean
 * @Order(100)
 * public NomosConfigurer advancedConfigurer() {
 *   return (functionRegistry, actionRegistry) -> {
 *     // Register advanced functions/actions that may depend on base ones
 *   };
 * }
 * }</pre>
 */
@FunctionalInterface
public interface NomosConfigurer extends Ordered {

  /**
   * Configure function and action registries before rule loading.
   *
   * @param functionRegistry Function registry to register custom functions
   * @param actionRegistry Action registry to register custom actions
   */
  void configure(FunctionRegistry functionRegistry, ActionRegistry actionRegistry);

  /**
   * Get the order value of this configurer.
   *
   * <p>Lower values have higher priority. The default is {@link Ordered#LOWEST_PRECEDENCE} to
   * ensure backwards compatibility.
   *
   * @return the order value
   */
  @Override
  default int getOrder() {
    return LOWEST_PRECEDENCE;
  }
}
