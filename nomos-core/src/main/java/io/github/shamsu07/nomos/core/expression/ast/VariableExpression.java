package io.github.shamsu07.nomos.core.expression.ast;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import java.util.Objects;

/**
 * Represents a variable access in an expression (e.g., "user", "cart.total").
 *
 * <p>Supports dot notation for nested property access.
 */
public final class VariableExpression implements Expression {

  private final String name;

  public VariableExpression(String name) {
    this.name = Objects.requireNonNull(name, "Variable name cannot be null");
  }

  @Override
  public Object evaluate(Facts facts, FunctionRegistry functionRegistry) {
    return facts.get(name);
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return String.format("Variable[%s]", name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VariableExpression)) {
      return false;
    }
    VariableExpression other = (VariableExpression) obj;
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
