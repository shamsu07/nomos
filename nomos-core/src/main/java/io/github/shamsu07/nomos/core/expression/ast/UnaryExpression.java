package io.github.shamsu07.nomos.core.expression.ast;

import io.github.shamsu07.nomos.core.expression.TokenType;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import java.util.Objects;

/**
 * Represents a unary operation in an expression (e.g., "!active", "-value").
 *
 * <p>Supports logical NOT and arithmetic negation.
 */
public final class UnaryExpression implements Expression {

  private final TokenType operator;
  private final Expression operand;

  public UnaryExpression(TokenType operator, Expression operand) {
    this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
    this.operand = Objects.requireNonNull(operand, "Operand cannot be null");
  }

  @Override
  public Object evaluate(Facts facts, FunctionRegistry functionRegistry) {
    Object value = operand.evaluate(facts, functionRegistry);

    switch (operator) {
      case NOT:
        return !toBoolean(value);
      case MINUS:
        return negate(value);
      case PLUS:
        return toNumber(value);
      default:
        throw new IllegalArgumentException("Unsupported unary operator: " + operator);
    }
  }

  private boolean toBoolean(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    throw new IllegalArgumentException("Cannot convert to boolean: " + value);
  }

  private Object negate(Object value) {
    if (value instanceof Number) {
      return -((Number) value).doubleValue();
    }
    throw new IllegalArgumentException("Cannot negate " + value.getClass());
  }

  private Object toNumber(Object value) {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    throw new IllegalArgumentException("Cannot apply unary + to " + value.getClass());
  }

  public TokenType getOperator() {
    return operator;
  }

  public Expression getOperand() {
    return operand;
  }

  @Override
  public String toString() {
    return String.format("Unary[%s %s]", operator, operand);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UnaryExpression)) {
      return false;
    }
    UnaryExpression other = (UnaryExpression) obj;
    return operator == other.operator && operand.equals(other.operand);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operator, operand);
  }
}
