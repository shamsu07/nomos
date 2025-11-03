package io.github.shamsu07.nomos.core.expression.ast;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import java.util.Objects;

/**
 * Represents a literal value in an expression (number, string, boolean, null).
 *
 * <p>Immutable AST node.
 */
public final class LiteralExpression implements Expression {

  private final Object value;

  public LiteralExpression(Object value) {
    this.value = value;
  }

  @Override
  public Object evaluate(Facts facts, FunctionRegistry functionRegistry) {
    return value;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("Literal[%s]", value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LiteralExpression)) {
      return false;
    }
    LiteralExpression other = (LiteralExpression) obj;
    return Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
