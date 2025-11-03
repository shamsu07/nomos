package io.github.shamsu07.nomos.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.action.NomosAction;
import io.github.shamsu07.nomos.core.engine.RuleEngine;
import io.github.shamsu07.nomos.core.expression.ExpressionEvaluator;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegrationTest {

  private FunctionRegistry functionRegistry;
  private ActionRegistry actionRegistry;
  private ExpressionEvaluator evaluator;
  private RuleEngine engine;
  private TestActions testActions;

  @BeforeEach
  void setup() {
    functionRegistry = new FunctionRegistry();
    actionRegistry = new ActionRegistry();
    testActions = new TestActions();

    functionRegistry.registerFunctionsFrom(new TestFunctions());
    actionRegistry.registerActionsFrom(testActions);

    evaluator = new ExpressionEvaluator(functionRegistry);
    engine = new RuleEngine();
  }

  @Test
  void should_evaluateExpressionWithFunction_when_integratedWithRegistry() {
    Facts facts = new Facts().put("user", new User("VIP", 100));
    Object result = evaluator.evaluate("isVIP()", facts);
    assertEquals(true, result);
  }

  @Test
  void should_evaluateComplexExpression_when_mixedOperations() {
    Facts facts = new Facts().put("user", new User("VIP", 150)).put("threshold", 100.0);
    Object result = evaluator.evaluate("isVIP() && user.balance > threshold", facts);
    assertEquals(true, result);
  }

  @Test
  void should_executeRuleWithDynamicCondition_when_expressionEvaluated() {
    Rule rule =
        Rule.builder()
            .name("VIP Discount")
            .when(
                facts -> {
                  Object result = evaluator.evaluate("isVIP() && user.balance > 100", facts);
                  return (Boolean) result;
                })
            .then(facts -> facts.put("discount", 10.0))
            .build();

    Facts input = new Facts().put("user", new User("VIP", 150));
    Facts result = rule.execute(input);
    assertEquals(10.0, result.get("discount"));
  }

  @Test
  void should_executeRuleWithAction_when_actionRegistered() {
    Rule rule =
        Rule.builder()
            .name("Send Email")
            .when(facts -> true)
            .then(actionRegistry.createAction("sendEmail", "test@example.com", "Hello"))
            .build();

    engine.addRule(rule);
    engine.execute(new Facts());

    assertEquals("test@example.com", testActions.lastEmail);
    assertEquals("Hello", testActions.lastMessage);
  }

  @Test
  void should_executeMultipleRules_when_priorityOrdered() {
    Rule highPriority =
        Rule.builder()
            .name("High")
            .priority(100)
            .when(facts -> (Boolean) evaluator.evaluate("isVIP()", facts))
            .then(actionRegistry.createAction("addToLog", "HIGH_PRIORITY"))
            .build();

    Rule lowPriority =
        Rule.builder()
            .name("Low")
            .priority(50)
            .when(facts -> true)
            .then(actionRegistry.createAction("addToLog", "LOW_PRIORITY"))
            .build();

    engine.addRule(lowPriority);
    engine.addRule(highPriority);

    Facts input = new Facts().put("user", new User("VIP", 100));
    engine.execute(input);

    assertEquals(2, testActions.logEntries.size());
    assertEquals("HIGH_PRIORITY", testActions.logEntries.get(0));
    assertEquals("LOW_PRIORITY", testActions.logEntries.get(1));
  }

  @Test
  void should_modifyFacts_when_actionExecutes() {
    Rule rule =
        Rule.builder()
            .name("Calculate Discount")
            .when(facts -> (Boolean) evaluator.evaluate("user.balance > 100", facts))
            .then(actionRegistry.createAction("applyDiscount", 15.0))
            .build();

    engine.addRule(rule);

    Facts input = new Facts().put("user", new User("Regular", 150));
    Facts result = engine.execute(input);

    assertEquals(15.0, result.get("discount"));
    assertEquals("DISCOUNT_APPLIED", result.get("status"));
  }

  @Test
  void should_chainMultipleActions_when_ruleExecutes() {
    Rule rule =
        Rule.builder()
            .name("VIP Processing")
            .when(facts -> (Boolean) evaluator.evaluate("isVIP()", facts))
            .then(actionRegistry.createAction("applyDiscount", 20.0))
            .then(
                actionRegistry.createAction("sendEmail", "vip@example.com", "VIP Discount Applied"))
            .then(actionRegistry.createAction("addToLog", "VIP_PROCESSED"))
            .build();

    Facts input = new Facts().put("user", new User("VIP", 200));
    Facts result = rule.execute(input);

    assertEquals(20.0, result.get("discount"));
    assertEquals("vip@example.com", testActions.lastEmail);
    assertEquals(1, testActions.logEntries.size());
  }

  @Test
  void should_evaluateNestedProperties_when_complexFacts() {
    User user = new User("Regular", 50);
    user.setAddress(new Address("NYC", "NY"));

    Facts facts = new Facts().put("user", user);
    Object result = evaluator.evaluate("user.address.city == \"NYC\"", facts);
    assertEquals(true, result);
  }

  @Test
  void should_executeRuleWithNestedPropertyCondition_when_integrated() {
    User user = new User("Regular", 50);
    user.setAddress(new Address("NYC", "NY"));

    Rule rule =
        Rule.builder()
            .name("NYC Discount")
            .when(facts -> (Boolean) evaluator.evaluate("user.address.city == \"NYC\"", facts))
            .then(facts -> facts.put("cityDiscount", 5.0))
            .build();

    Facts input = new Facts().put("user", user);
    Facts result = rule.execute(input);
    assertEquals(5.0, result.get("cityDiscount"));
  }

  @Test
  void should_skipRule_when_conditionFalse() {
    Rule rule =
        Rule.builder()
            .name("VIP Only")
            .when(facts -> (Boolean) evaluator.evaluate("isVIP()", facts))
            .then(facts -> facts.put("vipBonus", 100))
            .build();

    engine.addRule(rule);

    Facts input = new Facts().put("user", new User("Regular", 50));
    Facts result = engine.execute(input);

    assertNull(result.get("vipBonus"));
  }

  @Test
  void should_executeFunctionWithFactsParameter_when_called() {
    Facts facts = new Facts().put("multiplier", 5);
    Object result = evaluator.evaluate("multiplyByFact(10)", facts);
    assertEquals(50, result);
  }

  @Test
  void should_executeActionWithFactsParameter_when_called() {
    Rule rule =
        Rule.builder()
            .name("Process with Facts")
            .when(facts -> true)
            .then(actionRegistry.createAction("processWithFacts", 42))
            .build();

    Facts input = new Facts().put("prefix", "Value: ");
    Facts result = rule.execute(input);

    assertEquals("Value: 42", testActions.lastProcessed);
  }

  @Test
  void should_traceExecution_when_executeWithTrace() {
    Rule rule1 =
        Rule.builder()
            .name("Rule1")
            .priority(100)
            .when(facts -> true)
            .then(facts -> facts.put("r1", true))
            .build();

    Rule rule2 =
        Rule.builder()
            .name("Rule2")
            .priority(50)
            .when(facts -> (Boolean) evaluator.evaluate("r1 == true", facts))
            .then(facts -> facts.put("r2", true))
            .build();

    engine.addRule(rule1);
    engine.addRule(rule2);

    RuleEngine.ExecutionResult result = engine.executeWithTrace(new Facts());

    assertEquals(2, result.getFiredRules().size());
    assertEquals("Rule1", result.getFiredRules().get(0));
    assertEquals("Rule2", result.getFiredRules().get(1));
    assertEquals(true, result.getFacts().get("r1"));
    assertEquals(true, result.getFacts().get("r2"));
  }

  @Test
  void should_handleComplexBusinessLogic_when_fullIntegration() {
    // Setup rules
    Rule vipCheck =
        Rule.builder()
            .name("VIP Check")
            .priority(100)
            .when(facts -> (Boolean) evaluator.evaluate("isVIP() && user.balance > 100", facts))
            .then(actionRegistry.createAction("applyDiscount", 20.0))
            .then(actionRegistry.createAction("addToLog", "VIP_DISCOUNT_APPLIED"))
            .build();

    Rule cityBonus =
        Rule.builder()
            .name("City Bonus")
            .priority(90)
            .when(
                facts ->
                    (Boolean)
                        evaluator.evaluate("user.address.city == \"NYC\" && discount > 0", facts))
            .then(
                facts -> {
                  double current = facts.get("discount", Double.class);
                  return facts.put("discount", current + 5.0);
                })
            .build();

    Rule finalNotification =
        Rule.builder()
            .name("Final Notification")
            .priority(50)
            .when(facts -> facts.get("discount") != null)
            .then(
                actionRegistry.createAction(
                    "sendEmail", "customer@example.com", "Discount Applied"))
            .build();

    engine.addRule(vipCheck);
    engine.addRule(cityBonus);
    engine.addRule(finalNotification);

    // Execute
    User user = new User("VIP", 200);
    user.setAddress(new Address("NYC", "NY"));
    Facts input = new Facts().put("user", user);

    RuleEngine.ExecutionResult result = engine.executeWithTrace(input);

    // Verify
    assertEquals(3, result.getFiredRules().size());
    assertEquals(25.0, result.getFacts().get("discount")); // 20 + 5
    assertEquals("customer@example.com", testActions.lastEmail);
    assertTrue(testActions.logEntries.contains("VIP_DISCOUNT_APPLIED"));
  }

  @Test
  void should_handleArithmeticInExpression_when_calculating() {
    Facts facts = new Facts().put("price", 100.0).put("taxRate", 0.08);
    Object result = evaluator.evaluate("price * (1 + taxRate)", facts);
    assertEquals(108.0, result);
  }

  @Test
  void should_evaluateFunctionWithCalculation_when_combined() {
    Facts facts = new Facts().put("a", 5).put("b", 10);
    Object result = evaluator.evaluate("add(a, b) * 2", facts);
    assertEquals(30.0, result);
  }

  @Test
  void should_stopOnFirstRule_when_configured() {
    RuleEngine stopEngine = new RuleEngine(true);

    Rule first =
        Rule.builder()
            .name("First")
            .priority(100)
            .when(facts -> true)
            .then(facts -> facts.put("first", true))
            .build();

    Rule second =
        Rule.builder()
            .name("Second")
            .priority(50)
            .when(facts -> true)
            .then(facts -> facts.put("second", true))
            .build();

    stopEngine.addRule(first);
    stopEngine.addRule(second);

    Facts result = stopEngine.execute(new Facts());
    assertEquals(true, result.get("first"));
    assertNull(result.get("second"));
  }

  public static class TestFunctions {

    @NomosFunction("isVIP")
    public boolean isVIP(Facts facts) {
      User user = facts.get("user", User.class);
      return "VIP".equals(user.getType());
    }

    @NomosFunction("add")
    public int add(Object a, Object b) {
      return ((Number) a).intValue() + ((Number) b).intValue();
    }

    @NomosFunction("multiplyByFact")
    public int multiplyByFact(Facts facts, Object value) {
      int multiplier = facts.get("multiplier", Integer.class);
      return multiplier * ((Number) value).intValue();
    }
  }

  public static class TestActions {

    String lastEmail;
    String lastMessage;
    String lastProcessed;
    List<String> logEntries = new ArrayList<>();

    @NomosAction("sendEmail")
    public void sendEmail(String email, String message) {
      this.lastEmail = email;
      this.lastMessage = message;
    }

    @NomosAction("addToLog")
    public void addToLog(String entry) {
      this.logEntries.add(entry);
    }

    @NomosAction("applyDiscount")
    public Facts applyDiscount(Facts facts, double percent) {
      return facts.put("discount", percent).put("status", "DISCOUNT_APPLIED");
    }

    @NomosAction("processWithFacts")
    public void processWithFacts(Facts facts, Object value) {
      String prefix = facts.get("prefix", String.class);
      this.lastProcessed = prefix + value;
    }
  }

  public static class User {
    private String type;
    private double balance;
    private Address address;

    public User(String type, double balance) {
      this.type = type;
      this.balance = balance;
    }

    public String getType() {
      return type;
    }

    public double getBalance() {
      return balance;
    }

    public Address getAddress() {
      return address;
    }

    public void setAddress(Address address) {
      this.address = address;
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
