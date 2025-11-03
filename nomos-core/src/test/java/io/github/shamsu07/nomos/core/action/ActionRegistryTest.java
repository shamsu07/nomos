package io.github.shamsu07.nomos.core.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shamsu07.nomos.core.facts.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionRegistryTest {

  private ActionRegistry registry;

  @BeforeEach
  void setup() {
    registry = new ActionRegistry();
  }

  @Test
  void should_registerAction_when_annotationPresent() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    assertTrue(registry.hasAction("setDiscount"));
    assertTrue(registry.hasAction("logEvent"));
  }

  @Test
  void should_invokeVoidAction_when_registered() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    Facts input = new Facts();
    Facts result = registry.invoke("logEvent", input, "TEST_EVENT");

    // Void actions return facts unchanged
    assertEquals(input, result);
    assertEquals("TEST_EVENT", actions.lastLoggedEvent);
  }

  @Test
  void should_invokeFactsReturningAction_when_registered() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    Facts input = new Facts();
    Facts result = registry.invoke("setDiscount", input, 10.0);

    assertEquals(10.0, result.get("discount"));
  }

  @Test
  void should_invokeActionWithoutFactsParam_when_registered() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    Facts input = new Facts();
    Facts result = registry.invoke("sendEmail", input, "test@example.com", "Hello");

    assertEquals(input, result);
    assertEquals("test@example.com", actions.lastEmailRecipient);
    assertEquals("Hello", actions.lastEmailMessage);
  }

  @Test
  void should_throwException_when_duplicateActionRegistered() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    assertThrows(IllegalArgumentException.class, () -> registry.registerActionsFrom(actions));
  }

  @Test
  void should_throwException_when_actionNotFound() {
    assertThrows(ActionNotFoundException.class, () -> registry.getAction("nonExistent"));
  }

  @Test
  void should_throwException_when_argumentCountMismatch() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    assertThrows(
        RuntimeException.class, () -> registry.invoke("setDiscount", new Facts(), 10.0, 20.0));
  }

  @Test
  void should_removeAction_when_removeCalled() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    assertTrue(registry.removeAction("setDiscount"));
    assertFalse(registry.hasAction("setDiscount"));
  }

  @Test
  void should_returnFalse_when_removingNonExistentAction() {
    assertFalse(registry.removeAction("nonExistent"));
  }

  @Test
  void should_clearAllActions_when_clearCalled() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    assertEquals(3, registry.size());
    registry.clear();
    assertEquals(0, registry.size());
  }

  @Test
  void should_returnMetadata_when_getActionCalled() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    ActionMetadata metadata = registry.getAction("setDiscount");
    assertNotNull(metadata);
    assertEquals("setDiscount", metadata.getName());
    assertTrue(metadata.hasFactsParameter());
    assertEquals(Facts.class, metadata.getReturnType());
  }

  @Test
  void should_detectVoidReturnType_when_actionReturnsVoid() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    ActionMetadata metadata = registry.getAction("logEvent");
    assertTrue(metadata.returnsVoid());
  }

  @Test
  void should_createActionWrapper_when_createActionCalled() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    Facts input = new Facts();
    var action = registry.createAction("setDiscount", 15.0);
    Facts result = action.execute(input);

    assertEquals(15.0, result.get("discount"));
  }

  @Test
  void should_throwException_when_nullInstanceProvided() {
    assertThrows(NullPointerException.class, () -> registry.registerActionsFrom(null));
  }

  @Test
  void should_throwException_when_nullActionNameQueried() {
    assertThrows(NullPointerException.class, () -> registry.getAction(null));
  }

  @Test
  void should_throwException_when_nullActionNameRemoved() {
    assertThrows(NullPointerException.class, () -> registry.removeAction(null));
  }

  @Test
  void should_detectFactsParameter_when_firstParam() {
    TestActions actions = new TestActions();
    registry.registerActionsFrom(actions);

    ActionMetadata metadata1 = registry.getAction("setDiscount");
    assertTrue(metadata1.hasFactsParameter());

    ActionMetadata metadata2 = registry.getAction("sendEmail");
    assertFalse(metadata2.hasFactsParameter());
  }

  static class TestActions {

    String lastLoggedEvent;
    String lastEmailRecipient;
    String lastEmailMessage;

    @NomosAction("setDiscount")
    public Facts setDiscount(Facts facts, double percent) {
      return facts.put("discount", percent);
    }

    @NomosAction("logEvent")
    public void logEvent(Facts facts, String eventType) {
      this.lastLoggedEvent = eventType;
    }

    @NomosAction("sendEmail")
    public void sendEmail(String recipient, String message) {
      this.lastEmailRecipient = recipient;
      this.lastEmailMessage = message;
    }
  }
}
