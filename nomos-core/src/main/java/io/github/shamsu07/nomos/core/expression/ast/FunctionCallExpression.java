package io.github.shamsu07.nomos.core.expression.ast;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionMetadata;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a function call in an expression (e.g., "isVIP(user)").
 *
 * <p>Evaluates arguments first, then invokes function from registry.
 */
public final class FunctionCallExpression implements Expression {

  private final String functionName;
  private final List<Expression> arguments;

  public FunctionCallExpression(String functionName, List<Expression> arguments) {
    this.functionName = Objects.requireNonNull(functionName, "Function name cannot be null");
    this.arguments = List.copyOf(Objects.requireNonNull(arguments, "Arguments cannot be null"));
  }

  @Override
  public Object evaluate(Facts facts, FunctionRegistry functionRegistry) {
    // Evaluate all argument expressions
    List<Object> evaluatedArgs = new ArrayList<>(arguments.size());
    for (Expression arg : arguments) {
      evaluatedArgs.add(arg.evaluate(facts, functionRegistry));
    }

    // Get function metadata to check if Facts parameter needed
    FunctionMetadata metadata = functionRegistry.getFunction(functionName);

    // Build arguments array - inject Facts if function expects it
    Object[] invokeArgs;
    if (metadata.hasFactsParameter()) {
      invokeArgs = new Object[evaluatedArgs.size() + 1];
      invokeArgs[0] = facts;
      for (int i = 0; i < evaluatedArgs.size(); i++) {
        invokeArgs[i + 1] = evaluatedArgs.get(i);
      }
    } else {
      invokeArgs = evaluatedArgs.toArray();
    }

    // Invoke function with proper arguments
    return functionRegistry.invoke(functionName, invokeArgs);
  }

  public String getFunctionName() {
    return functionName;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return String.format("FunctionCall[%s, args=%s]", functionName, arguments);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FunctionCallExpression)) {
      return false;
    }
    FunctionCallExpression other = (FunctionCallExpression) obj;
    return functionName.equals(other.functionName) && arguments.equals(other.arguments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(functionName, arguments);
  }
}
