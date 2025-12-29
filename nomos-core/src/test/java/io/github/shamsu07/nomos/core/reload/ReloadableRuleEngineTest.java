package io.github.shamsu07.nomos.core.reload;

import static org.junit.jupiter.api.Assertions.*;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.action.NomosAction;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReloadableRuleEngineTest {

  @TempDir Path tempDir;

  private FunctionRegistry functionRegistry;
  private ActionRegistry actionRegistry;
  private ReloadableRuleEngine engine;

  @BeforeEach
  void setup() throws IOException {
    functionRegistry = new FunctionRegistry();
    actionRegistry = new ActionRegistry();

    functionRegistry.registerFunctionsFrom(new TestFunctions());
    actionRegistry.registerActionsFrom(new TestActions());

    engine = new ReloadableRuleEngine(functionRegistry, actionRegistry);
  }

  @AfterEach
  void cleanup() {
    if (engine != null) {
      engine.close();
    }
  }

  @Test
  void should_loadAndExecuteRules_when_validYAML() throws IOException {
    Path rulesFile = tempDir.resolve("rules.yml");
    String yaml =
        """
        rules:
          - name: "Test Rule"
            priority: 100
            when: "isTrue()"
            then:
              - result = 42
        """;
    Files.writeString(rulesFile, yaml);

    engine.loadRules(rulesFile.toString());

    Facts result = engine.execute(new Facts());
    assertEquals(42.0, result.get("result"));
  }

  @Test
  void should_reloadRules_when_manualReloadCalled() throws IOException {
    Path rulesFile = tempDir.resolve("rules.yml");
    String yaml1 =
        """
        rules:
          - name: "Rule 1"
            when: "isTrue()"
            then:
              - value = 1
        """;
    Files.writeString(rulesFile, yaml1);

    engine.loadRules(rulesFile.toString());
    Facts result1 = engine.execute(new Facts());
    assertEquals(1.0, result1.get("value"));

    // Modify rules
    String yaml2 =
        """
        rules:
          - name: "Rule 2"
            when: "isTrue()"
            then:
              - value = 2
        """;
    Files.writeString(rulesFile, yaml2);

    // Reload manually
    engine.reload();

    Facts result2 = engine.execute(new Facts());
    assertEquals(2.0, result2.get("value"));
  }

  @Test
  void should_autoReload_when_fileChanges() throws Exception {
    Path rulesFile = tempDir.resolve("rules.yml");
    String yaml1 =
        """
        rules:
          - name: "Rule 1"
            when: "isTrue()"
            then:
              - counter = 1
        """;
    Files.writeString(rulesFile, yaml1);

    CountDownLatch reloadLatch = new CountDownLatch(1);
    AtomicInteger reloadCount = new AtomicInteger(0);

    engine.setReloadListener(
        new ReloadableRuleEngine.ReloadListener() {
          @Override
          public void onReloadSuccess(int ruleCount, long durationMs) {
            reloadCount.incrementAndGet();
            reloadLatch.countDown();
          }

          @Override
          public void onReloadFailure(Exception error, long durationMs) {
            fail("Reload should not fail: " + error.getMessage());
          }
        });

    // Load with watching enabled
    engine.loadRules(rulesFile.toString(), true);

    // Initial execution
    Facts result1 = engine.execute(new Facts());
    assertEquals(1.0, result1.get("counter"));

    // Wait for watcher to fully start (WatchService registration can be slow)
    Thread.sleep(500);

    // Modify file
    String yaml2 =
        """
        rules:
          - name: "Rule 2"
            when: "isTrue()"
            then:
              - counter = 2
        """;
    Files.writeString(rulesFile, yaml2);

    // Force a file modification timestamp update to ensure WatchService detects the
    // change
    // Some filesystems have low-resolution timestamps that might not detect rapid
    // writes
    Files.setLastModifiedTime(
        rulesFile, java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis()));

    // Wait for auto-reload
    boolean reloaded = reloadLatch.await(5, TimeUnit.SECONDS);
    assertTrue(reloaded, "Rules should be auto-reloaded");

    // Give more time for engine swap to complete
    Thread.sleep(200);

    // Execute with new rules
    Facts result2 = engine.execute(new Facts());
    assertEquals(2.0, result2.get("counter"));
    assertTrue(reloadCount.get() >= 1, "Reload listener should be called");
  }

  @Test
  void should_keepOldRules_when_reloadFails() throws IOException {
    Path rulesFile = tempDir.resolve("rules.yml");
    String validYaml =
        """
        rules:
          - name: "Valid Rule"
            when: "isTrue()"
            then:
              - status = "ok"
        """;
    Files.writeString(rulesFile, validYaml);

    engine.loadRules(rulesFile.toString());

    // Verify initial rules work
    Facts result1 = engine.execute(new Facts());
    assertEquals("ok", result1.get("status"));

    // Write invalid YAML
    Files.writeString(rulesFile, "invalid: yaml: {{{}");

    // Reload should fail but not break engine
    assertThrows(IOException.class, () -> engine.reload());

    // Old rules should still work
    Facts result2 = engine.execute(new Facts());
    assertEquals("ok", result2.get("status"));
  }

  @Test
  void should_notifyListener_when_reloadSucceeds() throws IOException {
    Path rulesFile = tempDir.resolve("rules.yml");
    String yaml =
        """
        rules:
          - name: "Rule 1"
            when: "isTrue()"
            then:
              - x = 1
          - name: "Rule 2"
            when: "isTrue()"
            then:
              - y = 2
        """;
    Files.writeString(rulesFile, yaml);

    AtomicInteger ruleCount = new AtomicInteger(0);
    AtomicInteger durationMs = new AtomicInteger(0);

    engine.setReloadListener(
        new ReloadableRuleEngine.ReloadListener() {
          @Override
          public void onReloadSuccess(int count, long duration) {
            ruleCount.set(count);
            durationMs.set((int) duration);
          }

          @Override
          public void onReloadFailure(Exception error, long duration) {
            fail("Should not fail");
          }
        });

    engine.loadRules(rulesFile.toString());

    assertEquals(2, ruleCount.get(), "Should report 2 rules loaded");
    assertTrue(durationMs.get() >= 0, "Duration should be non-negative");
    assertTrue(durationMs.get() < 100, "Reload should be fast (<100ms)");
  }

  @Test
  void should_notifyListener_when_reloadFails() throws IOException {
    Path rulesFile = tempDir.resolve("rules.yml");
    Files.writeString(rulesFile, "valid: rule");

    AtomicInteger failureCount = new AtomicInteger(0);

    engine.setReloadListener(
        new ReloadableRuleEngine.ReloadListener() {
          @Override
          public void onReloadSuccess(int count, long duration) {
            fail("Should not succeed");
          }

          @Override
          public void onReloadFailure(Exception error, long duration) {
            failureCount.incrementAndGet();
          }
        });

    assertThrows(IOException.class, () -> engine.loadRules(rulesFile.toString()));
    assertEquals(1, failureCount.get(), "Failure listener should be called");
  }

  @Test
  void should_getRules_when_called() throws IOException {
    Path rulesFile = tempDir.resolve("rules.yml");
    String yaml =
        """
        rules:
          - name: "Rule A"
            when: "isTrue()"
            then:
              - x = 1
        """;
    Files.writeString(rulesFile, yaml);

    engine.loadRules(rulesFile.toString());

    assertEquals(1, engine.getRules().size());
    assertEquals("Rule A", engine.getRules().get(0).getName());
  }

  @Test
  void should_throwException_when_reloadCalledBeforeLoad() {
    assertThrows(IllegalStateException.class, () -> engine.reload());
  }

  public static class TestFunctions {
    @NomosFunction("isTrue")
    public boolean isTrue() {
      return true;
    }
  }

  public static class TestActions {
    @NomosAction("noop")
    public void noop() {}
  }
}
