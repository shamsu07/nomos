package io.github.shamsu07.nomos.core.action;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Objects;

/**
 * Metadata for a registered action including its MethodHandle and parameter types.
 *
 * <p>Immutable and thread-safe. Used by ActionRegistry to store action invocation details.
 */
public final class ActionMetadata {

  private final String name;
  private final MethodHandle methodHandle;
  private final Class<?>[] parameterTypes;
  private final Class<?> returnType;
  private final boolean hasFactsParameter;
  private final boolean returnsVoid;
  private final MethodHandle spreader;

  public ActionMetadata(
      String name,
      MethodHandle methodHandle,
      Class<?>[] parameterTypes,
      Class<?> returnType,
      boolean hasFactsParameter) {
    this.name = Objects.requireNonNull(name, "Action name cannot be null");
    this.methodHandle = Objects.requireNonNull(methodHandle, "MethodHandle cannot be null");
    this.parameterTypes = Objects.requireNonNull(parameterTypes, "Parameter types cannot be null");
    this.returnType = Objects.requireNonNull(returnType, "Return type cannot be null");
    this.hasFactsParameter = hasFactsParameter;
    this.returnsVoid = void.class.equals(returnType) || Void.class.equals(returnType);
    this.spreader = methodHandle.asSpreader(Object[].class, parameterTypes.length);
  }

  public String getName() {
    return name;
  }

  public MethodHandle getMethodHandle() {
    return methodHandle;
  }

  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }

  public MethodHandle getSpreader() {
    return spreader;
  }

  public Class<?> getReturnType() {
    return returnType;
  }

  public boolean hasFactsParameter() {
    return hasFactsParameter;
  }

  public boolean returnsVoid() {
    return returnsVoid;
  }

  public int getParameterCount() {
    return parameterTypes.length;
  }

  @Override
  public String toString() {
    return String.format(
        "ActionMetadata[name=%s, params=%s, return=%s]",
        name, Arrays.toString(parameterTypes), returnType.getSimpleName());
  }
}
