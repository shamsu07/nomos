package io.github.shamsu07.nomos.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.rule.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleEngineTest {
  private RuleEngine engine;

  @BeforeEach
  void setup() {
    engine = new RuleEngine();
  }

  @Test
  void should_executeMatchingRule_when_conditionTrue() {
    Rule rule =
        Rule.builder()
            .name("SetDiscount")
            .when(facts -> true)
            .then(facts -> facts.put("discount", 10))
            .build();

    engine.addRule(rule);
    Facts result = engine.execute(new Facts());
    assertEquals(10, result.get("discount"));
  }

  @Test
  void should_skipRule_when_conditionFalse() {
    Rule rule =
        Rule.builder()
            .name("VIP")
            .when(facts -> "VIP".equals(facts.get("type")))
            .then(facts -> facts.put("discount", 20))
            .build();
    engine.addRule(rule);
    Facts input = new Facts().put("type", "REGULAR");
    Facts result = engine.execute(input);
    assertNull(result.get("discount"));
  }

  @Test
  void should_executeInPriorityOrder_when_multipleRules() {
    Rule lowPriority =
        Rule.builder()
            .name("Low")
            .priority(10)
            .when(facts -> true)
            .then(facts -> facts.put("order", facts.get("order") + "L"))
            .build();
    Rule highPriority =
        Rule.builder()
            .name("High")
            .priority(100)
            .when(facts -> true)
            .then(facts -> facts.put("order", facts.get("order") + "H"))
            .build();

    engine.addRule(lowPriority);
    engine.addRule(highPriority);

    Facts input = new Facts().put("order", "");
    Facts result = engine.execute(input);
    assertEquals("HL", result.get("order"));
  }

  void should_stopAfterFirstRule_when_stopOnFirstEnabled() {
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

  @Test
  void should_removeRule_when_removeCalledWithName() {
    Rule rule = Rule.builder().name("Test").when(facts -> true).then(facts -> facts).build();
    engine.addRule(rule);
    assertTrue(engine.removeRule("Test"));
    assertTrue(engine.getRules().isEmpty());
  }

  @Test
  void should_returnFalse_when_removingNonExistentRule() {
    assertFalse(engine.removeRule("NonExistent"));
  }

  @Test
  void should_clearAllRules_when_clearCalled() {
    engine.addRule(Rule.builder().name("R1").when(facts -> true).then(facts -> facts).build());
    engine.addRule(Rule.builder().name("R2").when(facts -> true).then(facts -> facts).build());
    engine.clearRules();
    assertTrue(engine.getRules().isEmpty());
  }

  @Test
  void should_returnAllRules_when_getRulesCalled() {
    Rule r1 = Rule.builder().name("R1").when(facts -> true).then(facts -> facts).build();
    Rule r2 = Rule.builder().name("R2").when(facts -> true).then(facts -> facts).build();
    engine.addRule(r1);
    engine.addRule(r2);
    assertEquals(2, engine.getRules().size());
  }

  @Test
  void should_returnExecutionTrace_when_executeWithTraceCalled() {
    Rule rule1 =
        Rule.builder()
            .name("Rule1")
            .priority(100)
            .when(facts -> true)
            .then(facts -> facts.put("a", 1))
            .build();

    Rule rule2 =
        Rule.builder()
            .name("Rule2")
            .priority(50)
            .when(facts -> true)
            .then(facts -> facts.put("b", 2))
            .build();

    engine.addRule(rule1);
    engine.addRule(rule2);

    RuleEngine.ExecutionResult result = engine.executeWithTrace(new Facts());
    assertEquals(2, result.getFiredRules().size());
    assertEquals("Rule1", result.getFiredRules().get(0));
    assertEquals("Rule2", result.getFiredRules().get(1));
    assertEquals(1, result.getFacts().get("a"));
    assertEquals(2, result.getFacts().get("b"));
  }

  @Test
  void should_onlyTraceMatchingRules_when_someConditionsFail() {
    Rule matching = Rule.builder().name("Match").when(facts -> true).then(facts -> facts).build();

    Rule notMatching =
        Rule.builder().name("NoMatch").when(facts -> false).then(facts -> facts).build();

    engine.addRule(matching);
    engine.addRule(notMatching);

    RuleEngine.ExecutionResult result = engine.executeWithTrace(new Facts());
    assertEquals(1, result.getFiredRules().size());
    assertEquals("Match", result.getFiredRules().get(0));
  }

  @Test
  void should_throwException_when_nullFactsPassedToExecute() {
    assertThrows(NullPointerException.class, () -> engine.execute(null));
  }

  @Test
  void should_throwException_when_nullFactsPassedToExecuteWithTrace() {
    assertThrows(NullPointerException.class, () -> engine.executeWithTrace(null));
  }

  @Test
  void should_throwException_when_nullRuleAdded() {
    assertThrows(NullPointerException.class, () -> engine.addRule(null));
  }

  @Test
  void should_throwException_when_nullNamePassedToRemove() {
    assertThrows(NullPointerException.class, () -> engine.removeRule(null));
  }
}
