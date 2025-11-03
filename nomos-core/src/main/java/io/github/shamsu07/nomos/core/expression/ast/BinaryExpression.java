package io.github.shamsu07.nomos.core.expression.ast;

import io.github.shamsu07.nomos.core.expression.TokenType;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import java.util.Objects;

/**
 * Represents a binary operation in an expression (e.g., "a + b", "x > 10", "p && q").
 *
 * <p>Supports logical, comparison, and arithmetic opertors.
 */
public class BinaryExpression implements Expression {

  private final Expression left;
  private final TokenType operator;
  private final Expression right;

  public BinaryExpression(Expression left, TokenType operator, Expression right) {
    this.left = Objects.requireNonNull(left, "Left expression cannot be null");
    this.operator = Objects.requireNonNull(operator, "Operator cannot be null");
    this.right = Objects.requireNonNull(right, "Right expression cannot be null");
  }

  @Override
  public Object evaluate(Facts facts, FunctionRegistry functionRegistry) {
    Object leftValue = left.evaluate(facts, functionRegistry);
    Object rightValue = right.evaluate(facts, functionRegistry);

    switch (operator) {
      case AND:
        return toBoolean(leftValue) && toBoolean(rightValue);
      case OR:
        return toBoolean(leftValue) || toBoolean(rightValue);
      case EQUAL:
        return Objects.equals(leftValue, rightValue);
      case NOT_EQUAL:
        return !Objects.equals(leftValue, rightValue);
      case LESS:
        return compare(leftValue, rightValue) < 0;
      case GREATER:
        return compare(leftValue, rightValue) > 0;
      case LESS_EQUAL:
        return compare(leftValue, rightValue) <= 0;
      case GREATER_EQUAL:
        return compare(leftValue, rightValue) >= 0;
      case PLUS:
        return add(leftValue, rightValue);
      case MINUS:
        return subtract(leftValue, rightValue);
      case MULTIPLY:
        return multiply(leftValue, rightValue);
      case DIVIDE:
        return divide(leftValue, rightValue);
      default:
        throw new IllegalArgumentException("Unsupported binary operator: " + operator);
    }
  }

  private boolean toBoolean(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    throw new IllegalArgumentException("Cannot convert to boolean: " + value);
  }

  @SuppressWarnings("unchecked")
  private int compare(Object left, Object right) {
    if (left instanceof Comparable && right instanceof Comparable) {
      return ((Comparable<Object>) left).compareTo(right);
    }
    throw new IllegalArgumentException(
        String.format("Cannot compare %s and %s", left.getClass(), right.getClass()));
  }

  private Object add(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      return toDouble(left) + toDouble(right);
    }
    if (left instanceof String || right instanceof String) {
      return String.valueOf(left) + String.valueOf(right);
    }
    throw new IllegalArgumentException("Cannot add " + left.getClass() + "and" + right.getClass());
  }

  private Object subtract(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      return toDouble(left) - toDouble(right);
    }
    throw new IllegalArgumentException(
        "Cannot subtract " + left.getClass() + " and " + right.getClass());
  }

  private Object multiply(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      return toDouble(left) * toDouble(right);
    }
    throw new IllegalArgumentException(
        "Cannot multiply " + left.getClass() + " and " + right.getClass());
  }

  private Object divide(Object left, Object right) {
    if (left instanceof Number && right instanceof Number) {
      double divisor = toDouble(right);
      if (divisor == 0.0) {
        throw new ArithmeticException("Division by zero");
      }
      return toDouble(left) / divisor;
    }
    throw new IllegalArgumentException(
        "Cannot divide " + left.getClass() + " and " + right.getClass());
  }

  private double toDouble(Object value) {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    throw new IllegalArgumentException("Cannot convert to number: " + value);
  }

  public Expression getLeft() {
    return left;
  }

  public TokenType getOperator() {
    return operator;
  }

  public Expression getRight() {
    return right;
  }

  @Override
  public String toString() {
    return String.format("Binary[%s %s %s]", left, operator, right);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BinaryExpression)) {
      return false;
    }
    BinaryExpression other = (BinaryExpression) obj;
    return left.equals(other.left) && operator == other.operator && right.equals(other.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(left, operator, right);
  }
}
