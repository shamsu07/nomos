package io.github.shamsu07.nomos.benchmark;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.action.NomosAction;
import io.github.shamsu07.nomos.core.engine.RuleEngine;
import io.github.shamsu07.nomos.core.expression.ExpressionEvaluator;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import io.github.shamsu07.nomos.core.loader.YAMLRuleLoader;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks for rule execution performance.
 *
 * <p>Target: >100K rule evaluations per second
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class RuleExecutionBenchmark {

  private RuleEngine simpleEngine;
  private RuleEngine complexEngine;
  private Facts simpleFacts;
  private Facts complexFacts;

  @Setup
  public void setup() throws Exception {
    FunctionRegistry functionRegistry = new FunctionRegistry();
    ActionRegistry actionRegistry = new ActionRegistry();

    functionRegistry.registerFunctionsFrom(new BenchmarkFunctions());
    actionRegistry.registerActionsFrom(new BenchmarkActions());

    // Simple engine: 1 rule with basic condition
    simpleEngine = new RuleEngine();
    simpleEngine.addRule(
        Rule.builder()
            .name("Simple")
            .when(facts -> facts.get("value", Integer.class) > 10)
            .then(facts -> facts.put("result", "pass"))
            .build());

    simpleFacts = new Facts().put("value", 15);

    // Complex engine: 10 rules with expressions
    complexEngine = new RuleEngine();
    ExpressionEvaluator evaluator = new ExpressionEvaluator(functionRegistry);

    String yaml =
        """
        rules:
          - name: "Rule1"
            priority: 100
            when: "isVIP() && balance > 100"
            then:
              - discount = 10
          - name: "Rule2"
            priority: 90
            when: "balance > 500"
            then:
              - discount = 15
          - name: "Rule3"
            priority: 80
            when: "age >= 18 && age < 65"
            then:
              - eligible = true
          - name: "Rule4"
            priority: 70
            when: 'city == "NYC"'
            then:
              - tax = 8.875
          - name: "Rule5"
            priority: 60
            when: "orderCount > 10"
            then:
              - loyal = true
          - name: "Rule6"
            priority: 50
            when: "balance > 1000 && isVIP()"
            then:
              - discount = 20
          - name: "Rule7"
            priority: 40
            when: "age > 65"
            then:
              - senior = true
          - name: "Rule8"
            priority: 30
            when: 'city == "LA" || city == "SF"'
            then:
              - westCoast = true
          - name: "Rule9"
            priority: 20
            when: "balance < 100"
            then:
              - lowBalance = true
          - name: "Rule10"
            priority: 10
            when: "orderCount == 0"
            then:
              - firstTime = true
        """;

    YAMLRuleLoader loader = new YAMLRuleLoader(evaluator, functionRegistry, actionRegistry);
    List<Rule> rules = loader.load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));
    rules.forEach(complexEngine::addRule);

    complexFacts =
        new Facts()
            .put("vip", true)
            .put("balance", 150.0)
            .put("age", 30)
            .put("city", "NYC")
            .put("orderCount", 5);
  }

  @Benchmark
  public void simpleRuleExecution(Blackhole blackhole) {
    Facts result = simpleEngine.execute(simpleFacts);
    blackhole.consume(result);
  }

  @Benchmark
  public void complexRuleExecution(Blackhole blackhole) {
    Facts result = complexEngine.execute(complexFacts);
    blackhole.consume(result);
  }

  @Benchmark
  public void simpleRuleExecutionWithTrace(Blackhole blackhole) {
    RuleEngine.ExecutionResult result = simpleEngine.executeWithTrace(simpleFacts);
    blackhole.consume(result);
  }

  public static class BenchmarkFunctions {
    @NomosFunction("isVIP")
    public boolean isVIP(Facts facts) {
      return Boolean.TRUE.equals(facts.get("vip"));
    }
  }

  public static class BenchmarkActions {
    @NomosAction("noop")
    public void noop() {}
  }
}
