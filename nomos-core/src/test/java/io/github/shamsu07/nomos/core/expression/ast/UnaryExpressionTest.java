package io.github.shamsu07.nomos.core.expression.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.shamsu07.nomos.core.expression.TokenType;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import org.junit.jupiter.api.Test;

class UnaryExpressionTest {

  @Test
  void should_negateBoolean_when_notOperator() {
    UnaryExpression expr = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(false, result);
  }

  @Test
  void should_negateFalse_when_notOperator() {
    UnaryExpression expr = new UnaryExpression(TokenType.NOT, new LiteralExpression(false));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_negateNumber_when_minusOperator() {
    UnaryExpression expr = new UnaryExpression(TokenType.MINUS, new LiteralExpression(42.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(-42.0, result);
  }

  @Test
  void should_negateNegativeNumber_when_minusOperator() {
    UnaryExpression expr = new UnaryExpression(TokenType.MINUS, new LiteralExpression(-10.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(10.0, result);
  }

  @Test
  void should_negateVariable_when_minusOperator() {
    Facts facts = new Facts().put("value", 15.0);
    UnaryExpression expr = new UnaryExpression(TokenType.MINUS, new VariableExpression("value"));
    Object result = expr.evaluate(facts, new FunctionRegistry());
    assertEquals(-15.0, result);
  }

  @Test
  void should_negateExpression_when_notOperator() {
    BinaryExpression comparison =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.GREATER, new LiteralExpression(3.0));
    UnaryExpression expr = new UnaryExpression(TokenType.NOT, comparison);
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(false, result);
  }

  @Test
  void should_throwException_when_notOnNonBoolean() {
    UnaryExpression expr = new UnaryExpression(TokenType.NOT, new LiteralExpression(42));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_throwException_when_minusOnNonNumber() {
    UnaryExpression expr = new UnaryExpression(TokenType.MINUS, new LiteralExpression("text"));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_throwException_when_unsupportedOperator() {
    UnaryExpression expr = new UnaryExpression(TokenType.MULTIPLY, new LiteralExpression(42.0));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_returnNumber_when_plusOperator() {
    UnaryExpression expr = new UnaryExpression(TokenType.PLUS, new LiteralExpression(42.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(42.0, result);
  }

  @Test
  void should_throwException_when_plusOnNonNumber() {
    UnaryExpression expr = new UnaryExpression(TokenType.PLUS, new LiteralExpression("text"));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_returnOperator_when_getOperatorCalled() {
    UnaryExpression expr = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    assertEquals(TokenType.NOT, expr.getOperator());
  }

  @Test
  void should_returnOperand_when_getOperandCalled() {
    LiteralExpression operand = new LiteralExpression(42);
    UnaryExpression expr = new UnaryExpression(TokenType.MINUS, operand);
    assertEquals(operand, expr.getOperand());
  }

  @Test
  void should_beEqual_when_sameOperatorAndOperand() {
    UnaryExpression expr1 = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    UnaryExpression expr2 = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    assertEquals(expr1, expr2);
    assertEquals(expr1.hashCode(), expr2.hashCode());
  }

  @Test
  void should_notBeEqual_when_differentOperator() {
    UnaryExpression expr1 = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    UnaryExpression expr2 = new UnaryExpression(TokenType.MINUS, new LiteralExpression(42.0));
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_notBeEqual_when_differentOperand() {
    UnaryExpression expr1 = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    UnaryExpression expr2 = new UnaryExpression(TokenType.NOT, new LiteralExpression(false));
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_beEqual_when_sameInstance() {
    UnaryExpression expr = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    assertEquals(expr, expr);
  }

  @Test
  void should_notBeEqual_when_differentType() {
    UnaryExpression expr = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    assertNotEquals(expr, "not an expression");
  }

  @Test
  void should_haveCorrectToString_when_notOperator() {
    UnaryExpression expr = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    assertEquals("Unary[NOT Literal[true]]", expr.toString());
  }

  @Test
  void should_haveCorrectToString_when_minusOperator() {
    UnaryExpression expr = new UnaryExpression(TokenType.MINUS, new LiteralExpression(42));
    assertEquals("Unary[MINUS Literal[42]]", expr.toString());
  }

  @Test
  void should_handleNestedUnary_when_doubleNegation() {
    UnaryExpression inner = new UnaryExpression(TokenType.NOT, new LiteralExpression(true));
    UnaryExpression outer = new UnaryExpression(TokenType.NOT, inner);
    Object result = outer.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_handleNestedUnary_when_doubleMinusOnNumber() {
    UnaryExpression inner = new UnaryExpression(TokenType.MINUS, new LiteralExpression(5.0));
    UnaryExpression outer = new UnaryExpression(TokenType.MINUS, inner);
    Object result = outer.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(5.0, result);
  }
}
