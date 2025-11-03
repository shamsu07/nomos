package io.github.shamsu07.nomos.core.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionNotFoundException;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorTest {

  private FunctionRegistry functionRegistry;
  private ExpressionEvaluator evaluator;

  @BeforeEach
  void setup() {
    functionRegistry = new FunctionRegistry();
    functionRegistry.registerFunctionsFrom(new TestFunctions());
    evaluator = new ExpressionEvaluator(functionRegistry);
  }

  @Test
  void should_evaluateLiteral_when_number() {
    Object result = evaluator.evaluate("42", new Facts());
    assertEquals(42.0, result);
  }

  @Test
  void should_evaluateLiteral_when_string() {
    Object result = evaluator.evaluate("\"hello\"", new Facts());
    assertEquals("hello", result);
  }

  @Test
  void should_evaluateLiteral_when_true() {
    Object result = evaluator.evaluate("true", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateLiteral_when_false() {
    Object result = evaluator.evaluate("false", new Facts());
    assertEquals(false, result);
  }

  @Test
  void should_evaluateLiteral_when_null() {
    Object result = evaluator.evaluate("null", new Facts());
    assertNull(result);
  }

  @Test
  void should_evaluateVariable_when_simpleFact() {
    Facts facts = new Facts().put("name", "John");
    Object result = evaluator.evaluate("name", facts);
    assertEquals("John", result);
  }

  @Test
  void should_evaluateVariable_when_dotNotation() {
    User user = new User("Alice", 30);
    Facts facts = new Facts().put("user", user);
    Object result = evaluator.evaluate("user.name", facts);
    assertEquals("Alice", result);
  }

  @Test
  void should_evaluateVariable_when_nestedProperty() {
    User user = new User("Bob", 25);
    user.setAddress(new Address("NYC", "NY"));
    Facts facts = new Facts().put("user", user);
    Object result = evaluator.evaluate("user.address.city", facts);
    assertEquals("NYC", result);
  }

  @Test
  void should_evaluateFunctionCall_when_noArgs() {
    Object result = evaluator.evaluate("getConstant()", new Facts());
    assertEquals(100, result);
  }

  @Test
  void should_evaluateFunctionCall_when_withFactsArg() {
    Facts facts = new Facts().put("user", new User("VIP", 40));
    Object result = evaluator.evaluate("isVIP()", facts);
    assertEquals(true, result);
  }

  @Test
  void should_evaluateFunctionCall_when_withMultipleArgs() {
    Object result = evaluator.evaluate("add(5, 10)", new Facts());
    assertEquals(15, result);
  }

  @Test
  void should_evaluateArithmetic_when_addition() {
    Object result = evaluator.evaluate("5 + 3", new Facts());
    assertEquals(8.0, result);
  }

  @Test
  void should_evaluateArithmetic_when_subtraction() {
    Object result = evaluator.evaluate("10 - 4", new Facts());
    assertEquals(6.0, result);
  }

  @Test
  void should_evaluateArithmetic_when_multiplication() {
    Object result = evaluator.evaluate("6 * 7", new Facts());
    assertEquals(42.0, result);
  }

  @Test
  void should_evaluateArithmetic_when_division() {
    Object result = evaluator.evaluate("20 / 4", new Facts());
    assertEquals(5.0, result);
  }

  @Test
  void should_evaluateComparison_when_greater() {
    Object result = evaluator.evaluate("10 > 5", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateComparison_when_less() {
    Object result = evaluator.evaluate("3 < 8", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateComparison_when_equal() {
    Object result = evaluator.evaluate("5 == 5", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateComparison_when_notEqual() {
    Object result = evaluator.evaluate("5 != 3", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateComparison_when_lessEqual() {
    Object result = evaluator.evaluate("5 <= 5", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateComparison_when_greaterEqual() {
    Object result = evaluator.evaluate("6 >= 5", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateLogical_when_and() {
    Object result = evaluator.evaluate("true && true", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateLogical_when_or() {
    Object result = evaluator.evaluate("false || true", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateLogical_when_not() {
    Object result = evaluator.evaluate("!false", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateUnary_when_negation() {
    Object result = evaluator.evaluate("-5", new Facts());
    assertEquals(-5.0, result);
  }

  @Test
  void should_respectPrecedence_when_multiplicationBeforeAddition() {
    Object result = evaluator.evaluate("2 + 3 * 4", new Facts());
    assertEquals(14.0, result);
  }

  @Test
  void should_respectPrecedence_when_comparisonBeforeLogical() {
    Object result = evaluator.evaluate("5 > 3 && 2 < 4", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_overridePrecedence_when_parentheses() {
    Object result = evaluator.evaluate("(2 + 3) * 4", new Facts());
    assertEquals(20.0, result);
  }

  @Test
  void should_evaluateComplexExpression_when_mixed() {
    Facts facts = new Facts().put("user", new User("VIP", 40)).put("total", 150.0);
    Object result = evaluator.evaluate("isVIP() && total > 100", facts);
    assertEquals(true, result);
  }

  @Test
  void should_evaluateStringComparison_when_equal() {
    Facts facts = new Facts().put("status", "active");
    Object result = evaluator.evaluate("status == \"active\"", facts);
    assertEquals(true, result);
  }

  @Test
  void should_evaluateStringConcatenation_when_addition() {
    Object result = evaluator.evaluate("\"Hello\" + \" \" + \"World\"", new Facts());
    assertEquals("Hello World", result);
  }

  @Test
  void should_evaluateNestedFunctionCalls_when_present() {
    Object result = evaluator.evaluate("multiply(add(2, 3), 4)", new Facts());
    assertEquals(20, result);
  }

  @Test
  void should_evaluateWithVariables_when_arithmetic() {
    Facts facts = new Facts().put("a", 10.0).put("b", 5.0);
    Object result = evaluator.evaluate("a + b * 2", facts);
    assertEquals(20.0, result);
  }

  @Test
  void should_evaluateNullComparison_when_equal() {
    Facts facts = new Facts().put("value", null);
    Object result = evaluator.evaluate("value == null", facts);
    assertEquals(true, result);
  }

  @Test
  void should_evaluateNullComparison_when_notEqual() {
    Facts facts = new Facts().put("value", "test");
    Object result = evaluator.evaluate("value != null", facts);
    assertEquals(true, result);
  }

  @Test
  void should_returnNull_when_variableNotFound() {
    Object result = evaluator.evaluate("missing", new Facts());
    assertNull(result);
  }

  @Test
  void should_throwException_when_functionNotRegistered() {
    assertThrows(
        FunctionNotFoundException.class, () -> evaluator.evaluate("unknown()", new Facts()));
  }

  @Test
  void should_throwException_when_invalidSyntax() {
    assertThrows(ParseException.class, () -> evaluator.evaluate("a +", new Facts()));
  }

  @Test
  void should_throwException_when_divisionByZero() {
    assertThrows(RuntimeException.class, () -> evaluator.evaluate("10 / 0", new Facts()));
  }

  @Test
  void should_throwException_when_invalidComparison() {
    Facts facts = new Facts().put("a", "text");
    assertThrows(RuntimeException.class, () -> evaluator.evaluate("a > 5", facts));
  }

  @Test
  void should_throwException_when_invalidLogical() {
    Facts facts = new Facts().put("value", 123);
    assertThrows(RuntimeException.class, () -> evaluator.evaluate("value && true", facts));
  }

  @Test
  void should_evaluateBooleanProperty_when_isGetter() {
    User user = new User("Test", 20);
    user.setActive(true);
    Facts facts = new Facts().put("user", user);
    Object result = evaluator.evaluate("user.active", facts);
    assertEquals(true, result);
  }

  @Test
  void should_evaluateChainedComparisons_when_logicalOps() {
    Facts facts = new Facts().put("age", 25);
    Object result = evaluator.evaluate("age >= 18 && age < 65", facts);
    assertEquals(true, result);
  }

  @Test
  void should_evaluateComplexLogical_when_nested() {
    Object result = evaluator.evaluate("(true && false) || (true && true)", new Facts());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateFunctionWithVariables_when_mixed() {
    Facts facts = new Facts().put("x", 5).put("y", 10);
    Object result = evaluator.evaluate("add(x, y)", facts);
    assertEquals(15, result);
  }

  public static class TestFunctions {

    @NomosFunction("isVIP")
    public boolean isVIP(Facts facts) {
      User user = facts.get("user", User.class);
      return "VIP".equals(user.getName());
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
  }

  public static class User {
    private String name;
    private int age;
    private Address address;
    private boolean active;

    public User(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }

    public Address getAddress() {
      return address;
    }

    public void setAddress(Address address) {
      this.address = address;
    }

    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
    }
  }

  public static class Address {
    private String city;
    private String state;

    public Address(String city, String state) {
      this.city = city;
      this.state = state;
    }

    public String getCity() {
      return city;
    }

    public String getState() {
      return state;
    }
  }
}
