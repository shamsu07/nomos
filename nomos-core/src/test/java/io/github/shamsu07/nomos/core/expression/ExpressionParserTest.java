package io.github.shamsu07.nomos.core.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.shamsu07.nomos.core.expression.ast.BinaryExpression;
import io.github.shamsu07.nomos.core.expression.ast.Expression;
import io.github.shamsu07.nomos.core.expression.ast.FunctionCallExpression;
import io.github.shamsu07.nomos.core.expression.ast.LiteralExpression;
import io.github.shamsu07.nomos.core.expression.ast.UnaryExpression;
import io.github.shamsu07.nomos.core.expression.ast.VariableExpression;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExpressionParserTest {

  @Test
  void should_parseLiteral_when_number() {
    Expression expr = parse("42");
    assertInstanceOf(LiteralExpression.class, expr);
    assertEquals(42.0, ((LiteralExpression) expr).getValue());
  }

  @Test
  void should_parseLiteral_when_string() {
    Expression expr = parse("\"hello\"");
    assertInstanceOf(LiteralExpression.class, expr);
    assertEquals("hello", ((LiteralExpression) expr).getValue());
  }

  @Test
  void should_parseLiteral_when_true() {
    Expression expr = parse("true");
    assertInstanceOf(LiteralExpression.class, expr);
    assertEquals(true, ((LiteralExpression) expr).getValue());
  }

  @Test
  void should_parseLiteral_when_false() {
    Expression expr = parse("false");
    assertInstanceOf(LiteralExpression.class, expr);
    assertEquals(false, ((LiteralExpression) expr).getValue());
  }

  @Test
  void should_parseLiteral_when_null() {
    Expression expr = parse("null");
    assertInstanceOf(LiteralExpression.class, expr);
    assertEquals(null, ((LiteralExpression) expr).getValue());
  }

  @Test
  void should_parseVariable_when_identifier() {
    Expression expr = parse("user");
    assertInstanceOf(VariableExpression.class, expr);
    assertEquals("user", ((VariableExpression) expr).getName());
  }

  @Test
  void should_parseVariable_when_dotNotation() {
    Expression expr = parse("user.address.city");
    assertInstanceOf(VariableExpression.class, expr);
    assertEquals("user.address.city", ((VariableExpression) expr).getName());
  }

  @Test
  void should_parseFunctionCall_when_noArgs() {
    Expression expr = parse("func()");
    assertInstanceOf(FunctionCallExpression.class, expr);
    FunctionCallExpression funcCall = (FunctionCallExpression) expr;
    assertEquals("func", funcCall.getFunctionName());
    assertEquals(0, funcCall.getArguments().size());
  }

  @Test
  void should_parseFunctionCall_when_singleArg() {
    Expression expr = parse("isVIP(user)");
    assertInstanceOf(FunctionCallExpression.class, expr);
    FunctionCallExpression funcCall = (FunctionCallExpression) expr;
    assertEquals("isVIP", funcCall.getFunctionName());
    assertEquals(1, funcCall.getArguments().size());
    assertInstanceOf(VariableExpression.class, funcCall.getArguments().get(0));
  }

  @Test
  void should_parseFunctionCall_when_multipleArgs() {
    Expression expr = parse("add(1, 2, 3)");
    assertInstanceOf(FunctionCallExpression.class, expr);
    FunctionCallExpression funcCall = (FunctionCallExpression) expr;
    assertEquals("add", funcCall.getFunctionName());
    assertEquals(3, funcCall.getArguments().size());
  }

  @Test
  void should_parseUnaryExpression_when_not() {
    Expression expr = parse("!active");
    assertInstanceOf(UnaryExpression.class, expr);
    UnaryExpression unary = (UnaryExpression) expr;
    assertEquals(TokenType.NOT, unary.getOperator());
    assertInstanceOf(VariableExpression.class, unary.getOperand());
  }

  @Test
  void should_parseUnaryExpression_when_minus() {
    Expression expr = parse("-value");
    assertInstanceOf(UnaryExpression.class, expr);
    UnaryExpression unary = (UnaryExpression) expr;
    assertEquals(TokenType.MINUS, unary.getOperator());
    assertInstanceOf(VariableExpression.class, unary.getOperand());
  }

  @Test
  void should_parseBinaryExpression_when_addition() {
    Expression expr = parse("a + b");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.PLUS, binary.getOperator());
    assertInstanceOf(VariableExpression.class, binary.getLeft());
    assertInstanceOf(VariableExpression.class, binary.getRight());
  }

  @Test
  void should_parseBinaryExpression_when_multiplication() {
    Expression expr = parse("a * b");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.MULTIPLY, binary.getOperator());
  }

  @Test
  void should_parseBinaryExpression_when_comparison() {
    Expression expr = parse("x > 10");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.GREATER, binary.getOperator());
  }

  @Test
  void should_parseBinaryExpression_when_equality() {
    Expression expr = parse("status == 'active'");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.EQUAL, binary.getOperator());
  }

  @Test
  void should_parseBinaryExpression_when_and() {
    Expression expr = parse("a && b");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.AND, binary.getOperator());
  }

  @Test
  void should_parseBinaryExpression_when_or() {
    Expression expr = parse("a || b");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.OR, binary.getOperator());
  }

  @Test
  void should_respectPrecedence_when_multiplicationBeforeAddition() {
    Expression expr = parse("a + b * c");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.PLUS, binary.getOperator());
    assertInstanceOf(VariableExpression.class, binary.getLeft());
    assertInstanceOf(BinaryExpression.class, binary.getRight());
    BinaryExpression right = (BinaryExpression) binary.getRight();
    assertEquals(TokenType.MULTIPLY, right.getOperator());
  }

  @Test
  void should_respectPrecedence_when_comparisonBeforeLogical() {
    Expression expr = parse("x > 5 && y < 10");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.AND, binary.getOperator());
    assertInstanceOf(BinaryExpression.class, binary.getLeft());
    assertInstanceOf(BinaryExpression.class, binary.getRight());
  }

  @Test
  void should_respectPrecedence_when_andBeforeOr() {
    Expression expr = parse("a || b && c");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.OR, binary.getOperator());
    assertInstanceOf(VariableExpression.class, binary.getLeft());
    assertInstanceOf(BinaryExpression.class, binary.getRight());
    BinaryExpression right = (BinaryExpression) binary.getRight();
    assertEquals(TokenType.AND, right.getOperator());
  }

  @Test
  void should_handleParentheses_when_overridingPrecedence() {
    Expression expr = parse("(a + b) * c");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.MULTIPLY, binary.getOperator());
    assertInstanceOf(BinaryExpression.class, binary.getLeft());
    BinaryExpression left = (BinaryExpression) binary.getLeft();
    assertEquals(TokenType.PLUS, left.getOperator());
  }

  @Test
  void should_parseComplexExpression_when_mixed() {
    Expression expr = parse("isVIP(user) && cart.total > 100");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.AND, binary.getOperator());
    assertInstanceOf(FunctionCallExpression.class, binary.getLeft());
    assertInstanceOf(BinaryExpression.class, binary.getRight());
  }

  @Test
  void should_parseNestedFunctionCalls_when_present() {
    Expression expr = parse("outer(inner(x))");
    assertInstanceOf(FunctionCallExpression.class, expr);
    FunctionCallExpression outer = (FunctionCallExpression) expr;
    assertEquals("outer", outer.getFunctionName());
    assertEquals(1, outer.getArguments().size());
    assertInstanceOf(FunctionCallExpression.class, outer.getArguments().get(0));
  }

  @Test
  void should_parseChainedComparisons_when_present() {
    Expression expr = parse("a < b && b < c");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.AND, binary.getOperator());
  }

  @Test
  void should_parseNegatedExpression_when_notBeforeParens() {
    Expression expr = parse("!(a && b)");
    assertInstanceOf(UnaryExpression.class, expr);
    UnaryExpression unary = (UnaryExpression) expr;
    assertEquals(TokenType.NOT, unary.getOperator());
    assertInstanceOf(BinaryExpression.class, unary.getOperand());
  }

  @Test
  void should_throwException_when_missingRightParen() {
    assertThrows(ParseException.class, () -> parse("(a + b"));
  }

  @Test
  void should_throwException_when_missingOperand() {
    assertThrows(ParseException.class, () -> parse("a +"));
  }

  @Test
  void should_throwException_when_emptyExpression() {
    assertThrows(ParseException.class, () -> parse(""));
  }

  @Test
  void should_throwException_when_unexpectedToken() {
    assertThrows(ParseException.class, () -> parse(")"));
  }

  @Test
  void should_throwException_when_missingCommaInFunctionCall() {
    assertThrows(ParseException.class, () -> parse("func(a b)"));
  }

  @Test
  void should_throwException_when_dotWithoutIdentifier() {
    assertThrows(ParseException.class, () -> parse("user."));
  }

  @Test
  void should_parseMultipleArithmeticOps_when_leftAssociative() {
    Expression expr = parse("a - b - c");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.MINUS, binary.getOperator());
    assertInstanceOf(BinaryExpression.class, binary.getLeft());
    assertInstanceOf(VariableExpression.class, binary.getRight());
  }

  @Test
  void should_parseNotEqual_when_present() {
    Expression expr = parse("a != b");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.NOT_EQUAL, binary.getOperator());
  }

  @Test
  void should_parseLessEqual_when_present() {
    Expression expr = parse("a <= b");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.LESS_EQUAL, binary.getOperator());
  }

  @Test
  void should_parseGreaterEqual_when_present() {
    Expression expr = parse("a >= b");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.GREATER_EQUAL, binary.getOperator());
  }

  @Test
  void should_parseDivision_when_present() {
    Expression expr = parse("a / b");
    assertInstanceOf(BinaryExpression.class, expr);
    BinaryExpression binary = (BinaryExpression) expr;
    assertEquals(TokenType.DIVIDE, binary.getOperator());
  }

  private Expression parse(String input) {
    Lexer lexer = new Lexer(input);
    List<Token> tokens = lexer.tokenize();
    ExpressionParser parser = new ExpressionParser(tokens);
    return parser.parse();
  }
}
