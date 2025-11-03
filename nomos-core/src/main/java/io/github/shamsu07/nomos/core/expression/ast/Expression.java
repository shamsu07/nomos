package io.github.shamsu07.nomos.core.expression.ast;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;

/**
 * Base interface for all expression AST nodes.
 *
 * <p>Expressions are immutable and can be evaluated against Facts with a FunctionRegistry.
 */
public interface Expression {

  /**
   * Evaluate this expression against the given facts.
   *
   * @param facts Current facts
   * @param functionRegistry Registry for function calls
   * @return Evaluation result
   */
  Object evaluate(Facts facts, FunctionRegistry functionRegistry);
}
