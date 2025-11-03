package io.github.shamsu07.nomos.core.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shamsu07.nomos.core.facts.Facts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FunctionRegistryTest {

  private FunctionRegistry registry;

  @BeforeEach
  void setup() {
    registry = new FunctionRegistry();
  }

  @Test
  void should_registerFunction_when_annotationPresent() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    assertTrue(registry.hasFunction("isVIP"));
    assertTrue(registry.hasFunction("add"));
  }

  @Test
  void should_invokeFunctionCorrectly_when_registered() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    Facts facts = new Facts().put("user", new User("VIP"));
    Object result = registry.invoke("isVIP", facts);

    assertEquals(true, result);
  }

  @Test
  void should_invokeMultipleParamFunction_when_called() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    Object result = registry.invoke("add", 5, 10);
    assertEquals(15, result);
  }

  @Test
  void should_throwException_when_duplicateFunctionRegistered() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    assertThrows(IllegalArgumentException.class, () -> registry.registerFunctionsFrom(functions));
  }

  @Test
  void should_throwException_when_functionNotFound() {
    assertThrows(FunctionNotFoundException.class, () -> registry.getFunction("nonExistent"));
  }

  @Test
  void should_throwException_when_argumentCountMismatch() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    assertThrows(RuntimeException.class, () -> registry.invoke("add", 5));
  }

  @Test
  void should_removeFunction_when_removeCalled() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    assertTrue(registry.removeFunction("isVIP"));
    assertFalse(registry.hasFunction("isVIP"));
  }

  @Test
  void should_clearAllFunctions_when_clearCalled() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    assertEquals(3, registry.size());
    registry.clear();
    assertEquals(0, registry.size());
  }

  @Test
  void should_returnMetadata_when_getFuntionCalled() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    FunctionMetadata metadata = registry.getFunction("isVIP");
    assertNotNull(metadata);
    assertEquals("isVIP", metadata.getName());
    assertTrue(metadata.hasFactsParameter());
    assertEquals(boolean.class, metadata.getReturnType());
  }

  @Test
  void should_throwException_when_nullInstanceProvided() {
    assertThrows(NullPointerException.class, () -> registry.registerFunctionsFrom(null));
  }

  @Test
  void should_throwException_when_nullFunctionNameQueried() {
    assertThrows(NullPointerException.class, () -> registry.getFunction(null));
  }

  @Test
  void should_throwException_when_nullFunctionNameRemoved() {
    assertThrows(NullPointerException.class, () -> registry.removeFunction(null));
  }

  @Test
  void should_invokeFunctionWithoutFactsParam_when_registered() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    Object result = registry.invoke("multiply", 3, 4);
    assertEquals(12, result);
  }

  @Test
  void should_detectFactsParameter_when_firstParam() {
    TestFunctions functions = new TestFunctions();
    registry.registerFunctionsFrom(functions);

    FunctionMetadata metadata = registry.getFunction("isVIP");
    assertTrue(metadata.hasFactsParameter());

    FunctionMetadata metadata2 = registry.getFunction("add");
    assertFalse(metadata2.hasFactsParameter());
  }

  static class TestFunctions {

    @NomosFunction("isVIP")
    public boolean isVIP(Facts facts) {
      User user = facts.get("user", User.class);
      return "VIP".equals(user.getType());
    }

    @NomosFunction("add")
    public int add(int a, int b) {
      return a + b;
    }

    @NomosFunction("multiply")
    public int multiply(int a, int b) {
      return a * b;
    }
  }

  static class User {
    private final String type;

    public User(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }
}
