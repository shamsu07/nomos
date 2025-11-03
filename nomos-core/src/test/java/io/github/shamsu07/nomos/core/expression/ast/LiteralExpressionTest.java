package io.github.shamsu07.nomos.core.expression.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import org.junit.jupiter.api.Test;

class LiteralExpressionTest {

  @Test
  void should_evaluateToValue_when_number() {
    LiteralExpression expr = new LiteralExpression(42.0);
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(42.0, result);
  }

  @Test
  void should_evaluateToValue_when_string() {
    LiteralExpression expr = new LiteralExpression("hello");
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals("hello", result);
  }

  @Test
  void should_evaluateToValue_when_boolean() {
    LiteralExpression expr = new LiteralExpression(true);
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateToNull_when_null() {
    LiteralExpression expr = new LiteralExpression(null);
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertNull(result);
  }

  @Test
  void should_returnValue_when_getValueCalled() {
    LiteralExpression expr = new LiteralExpression(123);
    assertEquals(123, expr.getValue());
  }

  @Test
  void should_beEqual_when_sameValue() {
    LiteralExpression expr1 = new LiteralExpression(42);
    LiteralExpression expr2 = new LiteralExpression(42);
    assertEquals(expr1, expr2);
    assertEquals(expr1.hashCode(), expr2.hashCode());
  }

  @Test
  void should_notBeEqual_when_differentValue() {
    LiteralExpression expr1 = new LiteralExpression(42);
    LiteralExpression expr2 = new LiteralExpression(43);
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_beEqual_when_sameInstance() {
    LiteralExpression expr = new LiteralExpression(42);
    assertEquals(expr, expr);
  }

  @Test
  void should_notBeEqual_when_differentType() {
    LiteralExpression expr = new LiteralExpression(42);
    assertNotEquals(expr, "not an expression");
  }

  @Test
  void should_notBeEqual_when_nullValue() {
    LiteralExpression expr1 = new LiteralExpression(null);
    LiteralExpression expr2 = new LiteralExpression(42);
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_beEqual_when_bothNull() {
    LiteralExpression expr1 = new LiteralExpression(null);
    LiteralExpression expr2 = new LiteralExpression(null);
    assertEquals(expr1, expr2);
  }

  @Test
  void should_haveCorrectToString_when_numberValue() {
    LiteralExpression expr = new LiteralExpression(42);
    assertEquals("Literal[42]", expr.toString());
  }

  @Test
  void should_haveCorrectToString_when_stringValue() {
    LiteralExpression expr = new LiteralExpression("test");
    assertEquals("Literal[test]", expr.toString());
  }

  @Test
  void should_haveCorrectToString_when_nullValue() {
    LiteralExpression expr = new LiteralExpression(null);
    assertEquals("Literal[null]", expr.toString());
  }
}
