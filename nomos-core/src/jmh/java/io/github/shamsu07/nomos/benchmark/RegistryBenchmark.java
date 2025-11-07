package io.github.shamsu07.nomos.benchmark;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.action.NomosAction;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmarks for function and action registries.
 *
 * <p>Measures MethodHandle invocation overhead vs direct calls.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class RegistryBenchmark {

  private FunctionRegistry functionRegistry;
  private ActionRegistry actionRegistry;
  private Facts facts;
  private DirectCalls directCalls;

  @Setup
  public void setup() {
    functionRegistry = new FunctionRegistry();
    actionRegistry = new ActionRegistry();
    directCalls = new DirectCalls();

    BenchmarkFunctions functions = new BenchmarkFunctions();
    BenchmarkActions actions = new BenchmarkActions();

    functionRegistry.registerFunctionsFrom(functions);
    actionRegistry.registerActionsFrom(actions);

    facts = new Facts().put("value", 100);
  }

  @Benchmark
  public void directFunctionCall(Blackhole blackhole) {
    int result = directCalls.add(5, 10);
    blackhole.consume(result);
  }

  @Benchmark
  public void registryFunctionCall(Blackhole blackhole) {
    Object result = functionRegistry.invoke("add", 5, 10);
    blackhole.consume(result);
  }

  @Benchmark
  public void directFunctionCallWithFacts(Blackhole blackhole) {
    int result = directCalls.getValue(facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void registryFunctionCallWithFacts(Blackhole blackhole) {
    Object result = functionRegistry.invoke("getValue", facts);
    blackhole.consume(result);
  }

  @Benchmark
  public void directActionCall(Blackhole blackhole) {
    directCalls.noop();
    blackhole.consume(directCalls);
  }

  @Benchmark
  public void registryActionCall(Blackhole blackhole) {
    Facts result = actionRegistry.invoke("noop", facts);
    blackhole.consume(result);
  }

  public static class BenchmarkFunctions {
    @NomosFunction("add")
    public int add(int a, int b) {
      return a + b;
    }

    @NomosFunction("getValue")
    public int getValue(Facts facts) {
      return facts.get("value", Integer.class);
    }
  }

  public static class BenchmarkActions {
    @NomosAction("noop")
    public void noop() {}

    @NomosAction("setValue")
    public Facts setValue(Facts facts, int value) {
      return facts.put("result", value);
    }
  }

  public static class DirectCalls {
    public int add(int a, int b) {
      return a + b;
    }

    public int getValue(Facts facts) {
      return facts.get("value", Integer.class);
    }

    public void noop() {}
  }
}
