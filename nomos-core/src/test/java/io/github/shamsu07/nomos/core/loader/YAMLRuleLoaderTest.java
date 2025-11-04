package io.github.shamsu07.nomos.core.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.action.NomosAction;
import io.github.shamsu07.nomos.core.expression.ExpressionEvaluator;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YAMLRuleLoaderTest {

  private FunctionRegistry functionRegistry;
  private ActionRegistry actionRegistry;
  private ExpressionEvaluator evaluator;
  private YAMLRuleLoader loader;
  private TestActions testActions;

  @BeforeEach
  void setup() {
    functionRegistry = new FunctionRegistry();
    actionRegistry = new ActionRegistry();
    testActions = new TestActions();

    functionRegistry.registerFunctionsFrom(new TestFunctions());
    actionRegistry.registerActionsFrom(testActions);

    evaluator = new ExpressionEvaluator(functionRegistry);
    loader = new YAMLRuleLoader(evaluator, functionRegistry, actionRegistry);
  }

  @Test
  void should_loadSimpleRule_when_validYAML() {
    String yaml =
        """
            rules:
              - name: "Test Rule"
                priority: 100
                when: "true"
                then:
                  - discount = 10
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));

    assertEquals(1, rules.size());
    Rule rule = rules.get(0);
    assertEquals("Test Rule", rule.getName());
    assertEquals(100, rule.getPriority());
  }

  @Test
  void should_loadMultipleRules_when_validYAML() {
    String yaml =
        """
            rules:
              - name: "Rule 1"
                priority: 100
                when: "true"
                then:
                  - value = 1
              - name: "Rule 2"
                priority: 50
                when: "false"
                then:
                  - value = 2
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));

    assertEquals(2, rules.size());
    assertEquals("Rule 1", rules.get(0).getName());
    assertEquals("Rule 2", rules.get(1).getName());
  }

  @Test
  void should_parseAssignmentAction_when_simpleValue() {
    String yaml =
        """
            rules:
              - name: "Assignment Rule"
                when: "true"
                then:
                  - discount = 15
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    Facts input = new Facts();
    Facts result = rules.get(0).execute(input);

    assertEquals(15.0, result.get("discount"));
  }

  @Test
  void should_parseAssignmentAction_when_expression() {
    String yaml =
        """
            rules:
              - name: "Expression Assignment"
                when: "true"
                then:
                  - total = price * 1.1
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    assertEquals(1, rules.size());

    Facts input = new Facts().put("price", 100.0);
    Facts result = rules.get(0).execute(input);

    Object total = result.get("total");
    assertNotNull(total, "total should not be null");
    assertTrue(total instanceof Number, "total should be a Number, but was: " + total.getClass());
    assertEquals(110.0, ((Number) total).doubleValue(), 0.001, "Expected 110.0 but got: " + total);
  }

  @Test
  void should_parseAssignmentAction_when_nestedKey() {
    String yaml =
        """
            rules:
              - name: "Nested Assignment"
                when: "true"
                then:
                  - discount.percent = 20
                  - discount.reason = "VIP"
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    Facts result = rules.get(0).execute(new Facts());

    assertEquals(20.0, result.get("discount.percent"));
    assertEquals("VIP", result.get("discount.reason"));
  }

  @Test
  void should_parseFunctionCallAction_when_noArgs() {
    String yaml =
        """
            rules:
              - name: "No Args Action"
                when: "true"
                then:
                  - logEvent()
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    rules.get(0).execute(new Facts());

    assertEquals(1, testActions.logEntries.size());
    assertEquals("EVENT_LOGGED", testActions.logEntries.get(0));
  }

  @Test
  void should_parseFunctionCallAction_when_singleArg() {
    String yaml =
        """
            rules:
              - name: "Single Arg Action"
                when: "true"
                then:
                  - sendEmail("test@example.com")
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    rules.get(0).execute(new Facts());

    assertEquals("test@example.com", testActions.lastEmail);
  }

  @Test
  void should_parseFunctionCallAction_when_multipleArgs() {
    String yaml =
        """
            rules:
              - name: "Multiple Args Action"
                when: "true"
                then:
                  - addToLog("INFO", "Test message")
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    rules.get(0).execute(new Facts());

    assertEquals(1, testActions.logEntries.size());
    assertEquals("INFO: Test message", testActions.logEntries.get(0));
  }

  @Test
  void should_parseFunctionCallAction_when_variableArgs() {
    String yaml =
        """
            rules:
              - name: "Variable Args Action"
                when: "true"
                then:
                  - sendEmail(user.email)
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    Facts input = new Facts().put("user.email", "user@example.com");
    rules.get(0).execute(input);

    assertEquals("user@example.com", testActions.lastEmail);
  }

  @Test
  void should_evaluateCondition_when_functionCall() {
    String yaml =
        """
            rules:
              - name: "Function Condition"
                when: "isVIP()"
                then:
                  - discount = 20
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    Rule rule = rules.get(0);

    // Condition should evaluate to true for VIP
    Facts vipFacts = new Facts().put("user", new User("VIP"));
    assertTrue(rule.evaluate(vipFacts));

    // Condition should evaluate to false for Regular
    Facts regularFacts = new Facts().put("user", new User("Regular"));
    assertFalse(rule.evaluate(regularFacts));
  }

  @Test
  void should_executeMixedActions_when_assignmentsAndFunctions() {
    String yaml =
        """
            rules:
              - name: "Mixed Actions"
                when: "true"
                then:
                  - discount = 15
                  - status = "APPLIED"
                  - sendEmail("admin@example.com")
                  - addToLog("DISCOUNT", "15% applied")
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    Facts result = rules.get(0).execute(new Facts());

    assertEquals(15.0, result.get("discount"));
    assertEquals("APPLIED", result.get("status"));
    assertEquals("admin@example.com", testActions.lastEmail);
    assertTrue(testActions.logEntries.contains("DISCOUNT: 15% applied"));
  }

  @Test
  void should_usePriorityZero_when_notSpecified() {
    String yaml =
        """
            rules:
              - name: "No Priority"
                when: "true"
                then:
                  - value = 1
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    assertEquals(0, rules.get(0).getPriority());
  }

  @Test
  void should_throwException_when_missingRulesKey() {
    String yaml = "other: value";

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_missingName() {
    String yaml =
        """
            rules:
              - when: "true"
                then:
                  - value = 1
            """;

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_missingWhen() {
    String yaml =
        """
            rules:
              - name: "No When"
                then:
                  - value = 1
            """;

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_missingThen() {
    String yaml =
        """
            rules:
              - name: "No Then"
                when: "true"
            """;

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_emptyThen() {
    String yaml =
        """
            rules:
              - name: "Empty Then"
                when: "true"
                then: []
            """;

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_invalidWhenExpression() {
    String yaml =
        """
            rules:
              - name: "Invalid When"
                when: "invalid syntax &&"
                then:
                  - value = 1
            """;

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_invalidAssignmentValue() {
    String yaml =
        """
            rules:
              - name: "Invalid Assignment"
                when: "true"
                then:
                  - value = invalid syntax +
            """;

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_unknownAction() {
    String yaml =
        """
            rules:
              - name: "Unknown Action"
                when: "true"
                then:
                  - unknownFunction()
            """;

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_invalidActionSyntax() {
    String yaml =
        """
            rules:
              - name: "Invalid Action"
                when: "true"
                then:
                  - "not an assignment or function call"
            """;

    assertThrows(RuleParseException.class, () -> loader.load(toInputStream(yaml)));
  }

  @Test
  void should_throwException_when_nullInputStream() {
    assertThrows(NullPointerException.class, () -> loader.load(null));
  }

  @Test
  void should_throwException_when_nullEvaluator() {
    assertThrows(
        NullPointerException.class,
        () -> new YAMLRuleLoader(null, functionRegistry, actionRegistry));
  }

  @Test
  void should_throwException_when_nullFunctionRegistry() {
    assertThrows(
        NullPointerException.class, () -> new YAMLRuleLoader(evaluator, null, actionRegistry));
  }

  @Test
  void should_throwException_when_nullActionRegistry() {
    assertThrows(
        NullPointerException.class, () -> new YAMLRuleLoader(evaluator, functionRegistry, null));
  }

  @Test
  void should_returnEmptyList_when_emptyRulesArray() {
    String yaml = "rules: []";

    List<Rule> rules = loader.load(toInputStream(yaml));
    assertTrue(rules.isEmpty());
  }

  @Test
  void should_parseStringLiterals_when_inActions() {
    String yaml =
        """
            rules:
              - name: "String Literal"
                when: "true"
                then:
                  - message = "Hello World"
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    Facts result = rules.get(0).execute(new Facts());

    assertEquals("Hello World", result.get("message"));
  }

  @Test
  void should_parseNestedFunctionCalls_when_inArguments() {
    String yaml =
        """
            rules:
              - name: "Nested Call"
                when: "true"
                then:
                  - processValue(add(5, 10))
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    rules.get(0).execute(new Facts());

    assertEquals(15, testActions.lastProcessedValue);
  }

  @Test
  void should_handleComplexArguments_when_mixedTypes() {
    String yaml =
        """
            rules:
              - name: "Complex Args"
                when: "true"
                then:
                  - 'msg = "Message: " + "value"'
                  - addToLog("LEVEL", msg)
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    Facts result = rules.get(0).execute(new Facts());

    assertEquals("Message: value", result.get("msg"));
    assertEquals(1, testActions.logEntries.size());
    assertEquals("LEVEL: Message: value", testActions.logEntries.get(0));
  }

  @Test
  void should_includeRuleName_when_exceptionThrown() {
    String yaml =
        """
            rules:
              - name: "Problematic Rule"
                when: "invalid syntax"
                then:
                  - value = 1
            """;

    try {
      loader.load(toInputStream(yaml));
    } catch (RuleParseException e) {
      assertNotNull(e.getRuleName());
      assertEquals("Problematic Rule", e.getRuleName());
      assertTrue(e.getMessage().contains("Problematic Rule"));
    }
  }

  @Test
  void should_handlePriorityTypes_when_intOrDouble() {
    String yaml =
        """
            rules:
              - name: "Int Priority"
                priority: 100
                when: "true"
                then:
                  - value = 1
              - name: "Double Priority"
                priority: 99.5
                when: "true"
                then:
                  - value = 2
            """;

    List<Rule> rules = loader.load(toInputStream(yaml));
    assertEquals(100, rules.get(0).getPriority());
    assertEquals(99, rules.get(1).getPriority()); // Truncated to int
  }

  private InputStream toInputStream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }

  public static class TestFunctions {

    @NomosFunction("isVIP")
    public boolean isVIP(Facts facts) {
      User user = facts.get("user", User.class);
      return user != null && "VIP".equals(user.getType());
    }

    @NomosFunction("add")
    public int add(Object a, Object b) {
      return ((Number) a).intValue() + ((Number) b).intValue();
    }
  }

  public static class TestActions {
    String lastEmail;
    int lastProcessedValue;
    List<String> logEntries = new ArrayList<>();

    @NomosAction("sendEmail")
    public void sendEmail(String email) {
      this.lastEmail = email;
    }

    @NomosAction("logEvent")
    public void logEvent() {
      this.logEntries.add("EVENT_LOGGED");
    }

    @NomosAction("addToLog")
    public void addToLog(String level, String message) {
      this.logEntries.add(level + ": " + message);
    }

    @NomosAction("processValue")
    public void processValue(Object value) {
      this.lastProcessedValue = ((Number) value).intValue();
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
