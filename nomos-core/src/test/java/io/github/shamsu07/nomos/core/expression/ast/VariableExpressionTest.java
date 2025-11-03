package io.github.shamsu07.nomos.core.expression.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import org.junit.jupiter.api.Test;

class VariableExpressionTest {

  @Test
  void should_evaluateToFactValue_when_simpleName() {
    Facts facts = new Facts().put("name", "John");
    VariableExpression expr = new VariableExpression("name");
    Object result = expr.evaluate(facts, new FunctionRegistry());
    assertEquals("John", result);
  }

  @Test
  void should_evaluateToNull_when_variableNotFound() {
    VariableExpression expr = new VariableExpression("missing");
    Object result = expr.evaluate(new Facts(), new FunctionRegistry());
    assertNull(result);
  }

  @Test
  void should_evaluateNestedProperty_when_dotNotation() {
    User user = new User("Alice", 30);
    Facts facts = new Facts().put("user", user);
    VariableExpression expr = new VariableExpression("user.name");
    Object result = expr.evaluate(facts, new FunctionRegistry());
    assertEquals("Alice", result);
  }

  @Test
  void should_evaluateDeepNestedProperty_when_multipleDots() {
    User user = new User("Bob", 25);
    user.setAddress(new Address("NYC", "NY"));
    Facts facts = new Facts().put("user", user);
    VariableExpression expr = new VariableExpression("user.address.city");
    Object result = expr.evaluate(facts, new FunctionRegistry());
    assertEquals("NYC", result);
  }

  @Test
  void should_returnName_when_getNameCalled() {
    VariableExpression expr = new VariableExpression("user.name");
    assertEquals("user.name", expr.getName());
  }

  @Test
  void should_beEqual_when_sameName() {
    VariableExpression expr1 = new VariableExpression("user");
    VariableExpression expr2 = new VariableExpression("user");
    assertEquals(expr1, expr2);
    assertEquals(expr1.hashCode(), expr2.hashCode());
  }

  @Test
  void should_notBeEqual_when_differentName() {
    VariableExpression expr1 = new VariableExpression("user");
    VariableExpression expr2 = new VariableExpression("cart");
    assertNotEquals(expr1, expr2);
  }

  @Test
  void should_beEqual_when_sameInstance() {
    VariableExpression expr = new VariableExpression("test");
    assertEquals(expr, expr);
  }

  @Test
  void should_notBeEqual_when_differentType() {
    VariableExpression expr = new VariableExpression("test");
    assertNotEquals(expr, "not an expression");
  }

  @Test
  void should_haveCorrectToString_when_simpleName() {
    VariableExpression expr = new VariableExpression("user");
    assertEquals("Variable[user]", expr.toString());
  }

  @Test
  void should_haveCorrectToString_when_dotNotation() {
    VariableExpression expr = new VariableExpression("user.name");
    assertEquals("Variable[user.name]", expr.toString());
  }

  @Test
  void should_evaluateBooleanProperty_when_isGetter() {
    User user = new User("Test", 20);
    user.setActive(true);
    Facts facts = new Facts().put("user", user);
    VariableExpression expr = new VariableExpression("user.active");
    Object result = expr.evaluate(facts, new FunctionRegistry());
    assertEquals(true, result);
  }

  @Test
  void should_evaluateNumberProperty_when_getter() {
    User user = new User("Test", 25);
    Facts facts = new Facts().put("user", user);
    VariableExpression expr = new VariableExpression("user.age");
    Object result = expr.evaluate(facts, new FunctionRegistry());
    assertEquals(25, result);
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
