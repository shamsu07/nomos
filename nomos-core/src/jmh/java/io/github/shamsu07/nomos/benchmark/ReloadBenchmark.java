package io.github.shamsu07.nomos.benchmark;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import io.github.shamsu07.nomos.core.reload.ReloadableRuleEngine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks for hot reload performance.
 *
 * <p>Target: <50ms reload time
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class ReloadBenchmark {

  private ReloadableRuleEngine engine;
  private Path rulesFile;

  @Setup
  public void setup() throws IOException {
    FunctionRegistry functionRegistry = new FunctionRegistry();
    ActionRegistry actionRegistry = new ActionRegistry();

    functionRegistry.registerFunctionsFrom(new BenchmarkFunctions());

    engine = new ReloadableRuleEngine(functionRegistry, actionRegistry);

    rulesFile = Files.createTempFile("benchmark-rules", ".yml");
    String yaml =
        """
        rules:
          - name: "Rule1"
            priority: 100
            when: "isTrue()"
            then:
              - value = 1
          - name: "Rule2"
            priority: 90
            when: "isTrue()"
            then:
              - value = 2
          - name: "Rule3"
            priority: 80
            when: "isTrue()"
            then:
              - value = 3
          - name: "Rule4"
            priority: 70
            when: "isTrue()"
            then:
              - value = 4
          - name: "Rule5"
            priority: 60
            when: "isTrue()"
            then:
              - value = 5
        """;
    Files.writeString(rulesFile, yaml);

    engine.loadRules(rulesFile.toString());
  }

  @TearDown
  public void cleanup() throws IOException {
    engine.close();
    Files.deleteIfExists(rulesFile);
  }

  @Benchmark
  public void reloadRules(Blackhole blackhole) throws IOException {
    engine.reload();
    blackhole.consume(engine);
  }

  public static class BenchmarkFunctions {
    @NomosFunction("isTrue")
    public boolean isTrue() {
      return true;
    }
  }
}
