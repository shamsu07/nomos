package io.github.shamsu07.nomos.benchmark;

import io.github.shamsu07.nomos.core.expression.ExpressionEvaluator;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks for expression parsing and evaluation.
 *
 * <p>Measures overhead of custom expression engine vs pure Java predicates.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class ExpressionEvaluationBenchmark {

  private ExpressionEvaluator evaluator;
  private Facts facts;

  @Setup
  public void setup() {
    FunctionRegistry registry = new FunctionRegistry();
    registry.registerFunctionsFrom(new BenchmarkFunctions());
    evaluator = new ExpressionEvaluator(registry);

    facts = new Facts().put("age", 30).put("balance", 150.0).put("name", "John").put("vip", true);
  }

  @Benchmark
  public void literalExpression(Blackhole blackhole) {
    Object result = evaluator.evaluate("42", facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void variableExpression(Blackhole blackhole) {
    Object result = evaluator.evaluate("age", facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void arithmeticExpression(Blackhole blackhole) {
    Object result = evaluator.evaluate("age + 10", facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void comparisonExpression(Blackhole blackhole) {
    Object result = evaluator.evaluate("balance > 100", facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void logicalExpression(Blackhole blackhole) {
    Object result = evaluator.evaluate("age >= 18 && balance > 50", facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void functionCallExpression(Blackhole blackhole) {
    Object result = evaluator.evaluate("isVIP()", facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void complexExpression(Blackhole blackhole) {
    Object result =
        evaluator.evaluate("isVIP() && (balance > 100 || age > 25) && name != null", facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void nestedPropertyAccess(Blackhole blackhole) {
    Facts nestedFacts = new Facts().put("user", new User("John", 30));
    Object result = evaluator.evaluate("user.name", nestedFacts);
    blackhole.consume(result);
  }

  public static class BenchmarkFunctions {
    @NomosFunction("isVIP")
    public boolean isVIP(Facts facts) {
      return Boolean.TRUE.equals(facts.get("vip"));
    }
  }

  public static class User {
    private final String name;
    private final int age;

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
  }
}
