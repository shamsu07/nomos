package io.github.shamsu07.nomos.core.expression.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.shamsu07.nomos.core.expression.TokenType;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import org.junit.jupiter.api.Test;

class BinaryExpressionTest {

  @Test
  void should_performAddition_when_plusOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(3.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(8.0, result);
  }

  @Test
  void should_performSubtraction_when_minusOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(10.0), TokenType.MINUS, new LiteralExpression(4.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(6.0, result);
  }

  @Test
  void should_performMultiplication_when_multiplyOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(6.0), TokenType.MULTIPLY, new LiteralExpression(7.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(42.0, result);
  }

  @Test
  void should_performDivision_when_divideOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(20.0), TokenType.DIVIDE, new LiteralExpression(4.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(5.0, result);
  }

  @Test
  void should_throwException_when_divisionByZero() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(10.0), TokenType.DIVIDE, new LiteralExpression(0.0));
    assertThrows(
        ArithmeticException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_concatenateStrings_when_plusOperatorWithStrings() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression("Hello"), TokenType.PLUS, new LiteralExpression(" World"));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals("Hello World", result);
  }

  @Test
  void should_concatenate_when_stringAndNumber() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression("Value: "), TokenType.PLUS, new LiteralExpression(42));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals("Value: 42", result);
  }

  @Test
  void should_compareEqual_when_equalOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.EQUAL, new LiteralExpression(5.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_compareNotEqual_when_notEqualOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.NOT_EQUAL, new LiteralExpression(3.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_compareLess_when_lessOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(3.0), TokenType.LESS, new LiteralExpression(5.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_compareGreater_when_greaterOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(10.0), TokenType.GREATER, new LiteralExpression(5.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_compareLessEqual_when_lessEqualOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.LESS_EQUAL, new LiteralExpression(5.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_compareGreaterEqual_when_greaterEqualOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(6.0), TokenType.GREATER_EQUAL, new LiteralExpression(5.0));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_performLogicalAnd_when_andOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(true), TokenType.AND, new LiteralExpression(true));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_returnFalse_when_andWithFalse() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(true), TokenType.AND, new LiteralExpression(false));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(false, result);
  }

  @Test
  void should_performLogicalOr_when_orOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(false), TokenType.OR, new LiteralExpression(true));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_returnFalse_when_orWithBothFalse() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(false), TokenType.OR, new LiteralExpression(false));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(false, result);
  }

  @Test
  void should_compareStrings_when_equalOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression("test"), TokenType.EQUAL, new LiteralExpression("test"));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_handleNull_when_equalOperator() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(null), TokenType.EQUAL, new LiteralExpression(null));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_handleMixedNumberTypes_when_comparison() {
    Facts facts = new Facts().put("intVal", 25);
    BinaryExpression expr =
        new BinaryExpression(
            new VariableExpression("intVal"), TokenType.GREATER, new LiteralExpression(20.0));
    Object result = expr.evaluate(facts, new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_throwException_when_addingIncompatibleTypes() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(true));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_throwException_when_subtractingNonNumbers() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression("text"), TokenType.MINUS, new LiteralExpression("more"));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_throwException_when_multiplyingNonNumbers() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression("text"), TokenType.MULTIPLY, new LiteralExpression(5.0));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_throwException_when_dividingNonNumbers() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression("text"), TokenType.DIVIDE, new LiteralExpression(5.0));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_throwException_when_comparingIncompatibleTypes() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression("text"), TokenType.GREATER, new LiteralExpression(5.0));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_throwException_when_logicalOpOnNonBoolean() {
    BinaryExpression expr =
        new BinaryExpression(new LiteralExpression(42), TokenType.AND, new LiteralExpression(true));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_throwException_when_unsupportedOperator() {
    BinaryExpression expr =
        new BinaryExpression(new LiteralExpression(5.0), TokenType.DOT, new LiteralExpression(3.0));
    assertThrows(
        IllegalArgumentException.class, () -> expr.evaluate(new Facts(), new FunctionRegistry()));
  }

  @Test
  void should_returnLeft_when_getLeftCalled() {
    LiteralExpression left = new LiteralExpression(5.0);
    BinaryExpression expr = new BinaryExpression(left, TokenType.PLUS, new LiteralExpression(3.0));
    assertEquals(left, expr.getLeft());
  }

  @Test
  void should_returnRight_when_getRightCalled() {
    LiteralExpression right = new LiteralExpression(3.0);
    BinaryExpression expr = new BinaryExpression(new LiteralExpression(5.0), TokenType.PLUS, right);
    assertEquals(right, expr.getRight());
  }

  @Test
  void should_returnOperator_when_getOperatorCalled() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.MULTIPLY, new LiteralExpression(3.0));
    assertEquals(TokenType.MULTIPLY, expr.getOperator());
  }

  @Test
  void should_beEqual_when_sameComponents() {
    BinaryExpression expr1 =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(3.0));
    BinaryExpression expr2 =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(3.0));
    assertEquals(expr1, expr2);
    assertEquals(expr1.hashCode(), expr2.hashCode());
  }

  @Test
  void should_notBeEqual_when_differentOperator() {
    BinaryExpression expr1 =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(3.0));
    BinaryExpression expr2 =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.MINUS, new LiteralExpression(3.0));
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_notBeEqual_when_differentOperands() {
    BinaryExpression expr1 =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(3.0));
    BinaryExpression expr2 =
        new BinaryExpression(
            new LiteralExpression(4.0), TokenType.PLUS, new LiteralExpression(3.0));
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_beEqual_when_sameInstance() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(3.0));
    assertEquals(expr, expr);
  }

  @Test
  void should_notBeEqual_when_differentType() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(3.0));
    assertNotEquals(expr, "not an expression");
  }

  @Test
  void should_haveCorrectToString_when_binary() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(5.0), TokenType.PLUS, new LiteralExpression(3.0));
    assertEquals("Binary[Literal[5.0] PLUS Literal[3.0]]", expr.toString());
  }

  @Test
  void should_evaluateWithVariables_when_mixed() {
    Facts facts = new Facts().put("a", 10.0).put("b", 5.0);
    BinaryExpression expr =
        new BinaryExpression(
            new VariableExpression("a"), TokenType.MULTIPLY, new VariableExpression("b"));
    Object result = expr.evaluate(facts, new FunctionRegistry());
    assertEquals(50.0, result);
  }

  @Test
  void should_handleNestedExpressions_when_combined() {
    BinaryExpression inner =
        new BinaryExpression(
            new LiteralExpression(2.0), TokenType.PLUS, new LiteralExpression(3.0));
    BinaryExpression outer =
        new BinaryExpression(inner, TokenType.MULTIPLY, new LiteralExpression(4.0));
    Object result = outer.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(20.0, result);
  }

  @Test
  void should_shortCircuitAnd_when_leftIsFalse() {
    // Create an expression that would throw if evaluated
    Expression throwingExpr =
        new Expression() {
          @Override
          public Object evaluate(Facts facts, FunctionRegistry registry) {
            throw new RuntimeException("Right side should not be evaluated");
          }
        };

    BinaryExpression expr =
        new BinaryExpression(new LiteralExpression(false), TokenType.AND, throwingExpr);

    // Should return false without evaluating right side
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(false, result);
  }

  @Test
  void should_evaluateRightSideOfAnd_when_leftIsTrue() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(true), TokenType.AND, new LiteralExpression(false));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(false, result);
  }

  @Test
  void should_shortCircuitOr_when_leftIsTrue() {
    // Create an expression that would throw if evaluated
    Expression throwingExpr =
        new Expression() {
          @Override
          public Object evaluate(Facts facts, FunctionRegistry registry) {
            throw new RuntimeException("Right side should not be evaluated");
          }
        };

    BinaryExpression expr =
        new BinaryExpression(new LiteralExpression(true), TokenType.OR, throwingExpr);

    // Should return true without evaluating right side
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateRightSideOfOr_when_leftIsFalse() {
    BinaryExpression expr =
        new BinaryExpression(
            new LiteralExpression(false), TokenType.OR, new LiteralExpression(true));
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertEquals(true, result);
  }
}
