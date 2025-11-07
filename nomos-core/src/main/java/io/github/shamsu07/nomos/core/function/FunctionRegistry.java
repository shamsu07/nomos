package io.github.shamsu07.nomos.core.function;

import io.github.shamsu07.nomos.core.facts.Facts;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for functions callable from rule conditions.
 *
 * <p>Uses MethodHandles for high-performance invocation. Supports both annotation-based
 * registration and manual lambda registraion.
 *
 * <p>Functions are validated at registration time (fail-fast), not during execution.
 */
public final class FunctionRegistry {

  private final Map<String, FunctionMetadata> functions;
  private final MethodHandles.Lookup lookup;

  public FunctionRegistry() {
    this.functions = new ConcurrentHashMap<>();
    this.lookup = MethodHandles.lookup();
  }

  /**
   * Register all methods annotated with @NomosFunction form the given object.
   *
   * @param instance Object containing annotated methods
   * @throws IllegalArgumentException if duplicate function names found
   * @throws IllegalStateException if method access fails
   */
  public void registerFunctionsFrom(Object instance) {
    Objects.requireNonNull(instance, "Instance cannot be null");

    Class<?> clazz = instance.getClass();
    for (Method method : clazz.getDeclaredMethods()) {
      NomosFunction annotation = method.getAnnotation(NomosFunction.class);
      if (annotation != null) {
        String functionName = annotation.value();
        validateFunctionName(functionName);

        try {
          method.setAccessible(true);
          MethodHandle handle = lookup.unreflect(method).bindTo(instance);

          Class<?>[] paramTypes = method.getParameterTypes();
          Class<?> returnType = method.getReturnType();

          // Validate Facts parameter is first if present
          boolean hasFactsParam = paramTypes.length > 0 && Facts.class.equals(paramTypes[0]);

          FunctionMetadata metadata =
              new FunctionMetadata(functionName, handle, paramTypes, returnType, hasFactsParam);

          registerFunction(functionName, metadata);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(
              String.format(
                  "Cannot access method '%s' for function '%s'", method.getName(), functionName),
              e);
        }
      }
    }
  }

  /**
   * Register a function manually with explicit metadata
   *
   * @param name Function name
   * @param metadata Function metadata
   * @throws IllegalArgumentException if function name already registered
   */
  public void registerFunction(String name, FunctionMetadata metadata) {
    Objects.requireNonNull(name, "Function name cannot be null");
    Objects.requireNonNull(metadata, "Function Metadata cannot be null");

    if (functions.containsKey(name)) {
      throw new IllegalArgumentException(
          String.format("Functions '%s' is already registered", name));
    }
    functions.put(name, metadata);
  }

  /**
   * Get function metadata by name.
   *
   * @param name Function name
   * @return Function metadata
   * @throws FunctionNotFoundException if function not registered
   */
  public FunctionMetadata getFunction(String name) {
    Objects.requireNonNull(name, "Function name cannot be null");

    FunctionMetadata metadata = functions.get(name);
    if (metadata == null) {
      throw new FunctionNotFoundException(name);
    }

    return metadata;
  }

  /**
   * Check if function is registered
   *
   * @param name Function name
   * @return true if function exists
   */
  public boolean hasFunction(String name) {
    return functions.containsKey(name);
  }

  /**
   * Remove function from registry.
   *
   * @param name Function name
   * @return true if function was removed
   */
  public boolean removeFunction(String name) {
    Objects.requireNonNull(name, "Function name cannot be null");
    return functions.remove(name) != null;
  }

  /** Clear all registered functions. */
  public void clear() {
    functions.clear();
  }

  /**
   * Get count of registered functions.
   *
   * @return Number of functions
   */
  public int size() {
    return functions.size();
  }

  public Object invoke(String name, Object... args) {
    FunctionMetadata metadata = getFunction(name);

    try {
      return metadata.getSpreader().invoke(args);
    } catch (Throwable t) {
      throw new RuntimeException(
          String.format("Error invoking function '%s': %s", name, t.getMessage()), t);
    }
  }

  private void validateFunctionName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Function name cannot be null or empty");
    }
  }
}
