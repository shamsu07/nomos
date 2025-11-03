package io.github.shamsu07.nomos.core.expression.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.shamsu07.nomos.core.expression.TokenType;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionNotFoundException;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FunctionCallExpressionTest {

  private FunctionRegistry functionRegistry;

  @BeforeEach
  void setup() {
    functionRegistry = new FunctionRegistry();
    functionRegistry.registerFunctionsFrom(new TestFunctions());
  }

  @Test
  void should_invokeFunctionWithNoArgs_when_noArguments() {
    FunctionCallExpression expr = new FunctionCallExpression("getConstant", List.of());
    Object result = expr.evaluate(new Facts(), functionRegistry);
    assertEquals(100, result);
  }

  @Test
  void should_invokeFunctionWithFactsParam_when_registered() {
    Facts facts = new Facts().put("user", new User("VIP"));
    FunctionCallExpression expr = new FunctionCallExpression("isVIP", List.of());
    Object result = expr.evaluate(facts, functionRegistry);
    assertEquals(true, result);
  }

  @Test
  void should_invokeFunctionWithArgs_when_multipleArguments() {
    FunctionCallExpression expr =
        new FunctionCallExpression(
            "add", List.of(new LiteralExpression(5), new LiteralExpression(10)));
    Object result = expr.evaluate(new Facts(), functionRegistry);
    assertEquals(15, result);
  }

  @Test
  void should_evaluateArgumentExpressions_when_beforeInvocation() {
    Facts facts = new Facts().put("a", 3).put("b", 7);
    FunctionCallExpression expr =
        new FunctionCallExpression(
            "add", List.of(new VariableExpression("a"), new VariableExpression("b")));
    Object result = expr.evaluate(facts, functionRegistry);
    assertEquals(10, result);
  }

  @Test
  void should_invokeNestedFunctions_when_argumentIsFunction() {
    FunctionCallExpression inner =
        new FunctionCallExpression(
            "add", List.of(new LiteralExpression(2), new LiteralExpression(3)));
    FunctionCallExpression outer =
        new FunctionCallExpression("multiply", List.of(inner, new LiteralExpression(4)));
    Object result = outer.evaluate(new Facts(), functionRegistry);
    assertEquals(20, result);
  }

  @Test
  void should_handleComplexExpressions_when_argumentsAreBinary() {
    BinaryExpression binaryArg =
        new BinaryExpression(
            new LiteralExpression(10.0), TokenType.PLUS, new LiteralExpression(5.0));
    FunctionCallExpression expr =
        new FunctionCallExpression("multiply", List.of(binaryArg, new LiteralExpression(2)));
    Object result = expr.evaluate(new Facts(), functionRegistry);
    assertEquals(30, result);
  }

  @Test
  void should_throwException_when_functionNotFound() {
    FunctionCallExpression expr = new FunctionCallExpression("unknown", List.of());
    assertThrows(
        FunctionNotFoundException.class, () -> expr.evaluate(new Facts(), functionRegistry));
  }

  @Test
  void should_throwException_when_wrongArgumentCount() {
    FunctionCallExpression expr =
        new FunctionCallExpression("add", List.of(new LiteralExpression(5)));
    assertThrows(RuntimeException.class, () -> expr.evaluate(new Facts(), functionRegistry));
  }

  @Test
  void should_returnFunctionName_when_getFunctionNameCalled() {
    FunctionCallExpression expr = new FunctionCallExpression("testFunc", List.of());
    assertEquals("testFunc", expr.getFunctionName());
  }

  @Test
  void should_returnArguments_when_getArgumentsCalled() {
    List<Expression> args = List.of(new LiteralExpression(1), new LiteralExpression(2));
    FunctionCallExpression expr = new FunctionCallExpression("test", args);
    assertEquals(2, expr.getArguments().size());
    assertEquals(args, expr.getArguments());
  }

  @Test
  void should_returnImmutableList_when_getArgumentsCalled() {
    FunctionCallExpression expr =
        new FunctionCallExpression("test", List.of(new LiteralExpression(1)));
    assertThrows(
        UnsupportedOperationException.class,
        () -> expr.getArguments().add(new LiteralExpression(2)));
  }

  @Test
  void should_beEqual_when_sameFunctionAndArgs() {
    FunctionCallExpression expr1 =
        new FunctionCallExpression("add", List.of(new LiteralExpression(5)));
    FunctionCallExpression expr2 =
        new FunctionCallExpression("add", List.of(new LiteralExpression(5)));
    assertEquals(expr1, expr2);
    assertEquals(expr1.hashCode(), expr2.hashCode());
  }

  @Test
  void should_notBeEqual_when_differentFunctionName() {
    FunctionCallExpression expr1 =
        new FunctionCallExpression("add", List.of(new LiteralExpression(5)));
    FunctionCallExpression expr2 =
        new FunctionCallExpression("multiply", List.of(new LiteralExpression(5)));
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_notBeEqual_when_differentArguments() {
    FunctionCallExpression expr1 =
        new FunctionCallExpression("add", List.of(new LiteralExpression(5)));
    FunctionCallExpression expr2 =
        new FunctionCallExpression("add", List.of(new LiteralExpression(10)));
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_beEqual_when_sameInstance() {
    FunctionCallExpression expr = new FunctionCallExpression("test", List.of());
    assertEquals(expr, expr);
  }

  @Test
  void should_notBeEqual_when_differentType() {
    FunctionCallExpression expr = new FunctionCallExpression("test", List.of());
    assertNotEquals(expr, "not an expression");
  }

  @Test
  void should_haveCorrectToString_when_noArgs() {
    FunctionCallExpression expr = new FunctionCallExpression("func", List.of());
    assertEquals("FunctionCall[func, args=[]]", expr.toString());
  }

  @Test
  void should_haveCorrectToString_when_withArgs() {
    FunctionCallExpression expr =
        new FunctionCallExpression(
            "add", List.of(new LiteralExpression(5), new LiteralExpression(3)));
    assertEquals("FunctionCall[add, args=[Literal[5], Literal[3]]]", expr.toString());
  }

  @Test
  void should_passFactsToFunction_when_functionExpectsIt() {
    Facts facts = new Facts().put("multiplier", 3);
    FunctionCallExpression expr =
        new FunctionCallExpression("multiplyByFact", List.of(new LiteralExpression(10)));
    Object result = expr.evaluate(facts, functionRegistry);
    assertEquals(30, result);
  }

  @Test
  void should_evaluateWithMixedArgs_when_variablesAndLiterals() {
    Facts facts = new Facts().put("x", 15);
    FunctionCallExpression expr =
        new FunctionCallExpression(
            "subtract", List.of(new VariableExpression("x"), new LiteralExpression(5)));
    Object result = expr.evaluate(facts, functionRegistry);
    assertEquals(10, result);
  }

  public static class TestFunctions {

    @NomosFunction("isVIP")
    public boolean isVIP(Facts facts) {
      User user = facts.get("user", User.class);
      return "VIP".equals(user.getType());
    }

    @NomosFunction("getConstant")
    public int getConstant() {
      return 100;
    }

    @NomosFunction("add")
    public int add(Object a, Object b) {
      return ((Number) a).intValue() + ((Number) b).intValue();
    }

    @NomosFunction("multiply")
    public int multiply(Object a, Object b) {
      return ((Number) a).intValue() * ((Number) b).intValue();
    }

    @NomosFunction("subtract")
    public int subtract(Object a, Object b) {
      return ((Number) a).intValue() - ((Number) b).intValue();
    }

    @NomosFunction("multiplyByFact")
    public int multiplyByFact(Facts facts, Object value) {
      int multiplier = facts.get("multiplier", Integer.class);
      return multiplier * ((Number) value).intValue();
    }
  }

  public static class User {
    private final String type;

    public User(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }
}
