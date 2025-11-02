package io.github.shamsu07.nomos.core.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shamsu07.nomos.core.facts.Facts;
import org.junit.jupiter.api.Test;

class RuleTest {

  @Test
  void should_buildRule_when_allFieldsProvided() {
    Rule rule =
        Rule.builder()
            .name("Test Rule")
            .priority(10)
            .when(facts -> true)
            .then(facts -> facts.put("result", "executed"))
            .build();
    assertEquals("Test Rule", rule.getName());
    assertEquals(10, rule.getPriority());
  }

  @Test
  void should_usePriorityZero_when_notSpecified() {
    Rule rule = Rule.builder().name("Rule").when(facts -> true).then(facts -> facts).build();
    assertEquals(0, rule.getPriority());
  }

  @Test
  void should_evaluateTrue_when_conditionMatches() {
    Rule rule =
        Rule.builder()
            .name("VIP")
            .when(facts -> "VIP".equals(facts.get("type")))
            .then(facts -> facts)
            .build();
    Facts facts = new Facts().put("type", "VIP");
    assertTrue(rule.evaluate(facts));
  }

  @Test
  void should_evaluateFalse_when_conditionDoesNotMatch() {
    Rule rule =
        Rule.builder()
            .name("VIP")
            .when(facts -> "VIP".equals(facts.get("type")))
            .then(facts -> facts)
            .build();

    Facts facts = new Facts().put("type", "REGULAR");
    assertFalse(rule.evaluate(facts));
  }

  @Test
  void should_executeActions_when_ruleFires() {
    Rule rule =
        Rule.builder()
            .name("Discount")
            .when(facts -> true)
            .then(facts -> facts.put("discount", 10))
            .build();
    Facts input = new Facts();
    Facts result = rule.execute(input);
    assertEquals(10, result.get("discount"));
  }

  @Test
  void should_executeMultipleActions_when_chained() {
    Rule rule =
        Rule.builder()
            .name("Multi")
            .when(facts -> true)
            .then(facts -> facts.put("a", 1))
            .then(facts -> facts.put("b", 2))
            .build();
    Facts result = rule.execute(new Facts());
    assertEquals(1, result.get("a"));
    assertEquals(2, result.get("b"));
  }

  @Test
  void should_throwException_when_nameNotProvided() {
    assertThrows(
        NullPointerException.class,
        () -> Rule.builder().when(facts -> true).then(facts -> facts).build());
  }

  @Test
  void should_throwException_when_conditionNotProvided() {
    assertThrows(
        IllegalStateException.class,
        () -> Rule.builder().name("Rule").then(facts -> facts).build());
  }

  @Test
  void should_throwException_when_actionsNotProvided() {
    assertThrows(
        IllegalStateException.class, () -> Rule.builder().name("Rule").when(facts -> true).build());
  }

  @Test
  void should_throwException_when_evaluateCalledWithNull() {
    Rule rule = Rule.builder().name("Rule").when(facts -> true).then(facts -> facts).build();
    assertThrows(NullPointerException.class, () -> rule.evaluate(null));
  }

  @Test
  void should_throwException_when_executeCalledWithNull() {
    Rule rule = Rule.builder().name("Rule").when(facts -> true).then(facts -> facts).build();
    assertThrows(NullPointerException.class, () -> rule.execute(null));
  }

  @Test
  void should_storeExpressionString_when_whenStringUsed() {
    Rule rule = Rule.builder().name("Rule").when("user.type == 'VIP'").then(facts -> facts).build();
    assertEquals("user.type == 'VIP'", rule.getConditionExpression());
  }

  @Test
  void should_returnImmutableActionsList_when_getActionsCalled() {
    Rule rule = Rule.builder().name("Rule").when(facts -> true).then(facts -> facts).build();
    assertThrows(UnsupportedOperationException.class, () -> rule.getActions().add(facts -> facts));
  }
}
