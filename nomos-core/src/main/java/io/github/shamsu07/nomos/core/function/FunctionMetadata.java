package io.github.shamsu07.nomos.core.function;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;

/**
 * Metadata for a registered function including its MethodHandle and parameter types.
 *
 * <p>Immutable and thread-safe. Used by FunctionRegistry to store function invocation details.
 */
public final class FunctionMetadata {

  private final String name;
  private final MethodHandle methodHandle;
  private final Class<?>[] parameterTypes;
  private final Class<?> returnType;
  private final boolean hasFactsParameter;

  public FunctionMetadata(
      String name,
      MethodHandle methodHandle,
      Class<?>[] parameterTypes,
      Class<?> returnType,
      boolean hasFactsParameter) {
    this.name = Objects.requireNonNull(name, "Function name cannot be null");
    this.methodHandle = Objects.requireNonNull(methodHandle, "MethodHandle cannot be null");
    this.parameterTypes = Objects.requireNonNull(parameterTypes, "Parameter types cannot be null");
    this.returnType = Objects.requireNonNull(returnType, "Return type cannot be null");
    this.hasFactsParameter = hasFactsParameter;
  }

  public String getName() {
    return name;
  }

  public MethodHandle getMethodHandle() {
    return methodHandle;
  }

  public Class<?>[] getParameterTypes() {
    return Arrays.copyOf(parameterTypes, parameterTypes.length);
  }

  public Class<?> getReturnType() {
    return returnType;
  }

  public boolean hasFactsParameter() {
    return hasFactsParameter;
  }

  public int getParameterCount() {
    return parameterTypes.length;
  }

  @Override
  public String toString() {
    return String.format(
        "FunctionMetadata[name=%s, param=%s, return=%s]",
        name, Arrays.toString(parameterTypes), returnType.getSimpleName());
  }
}
