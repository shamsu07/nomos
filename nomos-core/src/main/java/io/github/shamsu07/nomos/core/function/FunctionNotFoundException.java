package io.github.shamsu07.nomos.core.function;

/**
 * Thrown when a function referenced in a rule expression is not registered in the function
 * registry.
 */
public class FunctionNotFoundException extends RuntimeException {

  private final String functionName;

  public FunctionNotFoundException(String functionName) {
    super(String.format("Function '%s' is not registered", functionName));
    this.functionName = functionName;
  }

  public FunctionNotFoundException(String functionName, String ruleName, int line) {
    super(
        String.format(
            "Function '%s' not found in rule '%s' (line %d)", functionName, ruleName, line));
    this.functionName = functionName;
  }

  public String getFunctionName() {
    return functionName;
  }
}
