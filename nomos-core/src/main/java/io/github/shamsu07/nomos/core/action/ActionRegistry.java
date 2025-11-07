package io.github.shamsu07.nomos.core.action;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for actions callable from rule then clauses.
 *
 * <p>Uses MethodHandles for high-performance invocation. Supports both annotation-based
 * registration and manual registration. Wraps void-returning methods to return Facts.
 */
public final class ActionRegistry {

  private final Map<String, ActionMetadata> actions;
  private final MethodHandles.Lookup lookup;

  public ActionRegistry() {
    this.actions = new ConcurrentHashMap<>();
    this.lookup = MethodHandles.lookup();
  }

  /**
   * Register all methods annotated with @NomosAction from the given object.
   *
   * @param instance Object containing annotated methods
   * @throws IllegalArgumentException if duplicate action names found
   * @throws IllegalStateException if method access fails
   */
  public void registerActionsFrom(Object instance) {
    Objects.requireNonNull(instance, "Instance cannot be null");

    Class<?> clazz = instance.getClass();
    for (Method method : clazz.getDeclaredMethods()) {
      NomosAction annotation = method.getAnnotation(NomosAction.class);
      if (annotation != null) {
        String actionName = annotation.value();
        validateActionName(actionName);
        try {
          method.setAccessible(true);
          MethodHandle handle = lookup.unreflect(method).bindTo(instance);

          Class<?>[] paramTypes = method.getParameterTypes();
          Class<?> returnType = method.getReturnType();

          // Validate Facts parameter is first if present
          boolean hasFactsParam = paramTypes.length > 0 && Facts.class.equals(paramTypes[0]);

          ActionMetadata metadata =
              new ActionMetadata(actionName, handle, paramTypes, returnType, hasFactsParam);
          registerAction(actionName, metadata);
        } catch (IllegalAccessException e) {
          throw new IllegalStateException(
              String.format(
                  "Cannot access method '%s' for action '%s'", method.getName(), actionName),
              e);
        }
      }
    }
  }

  /**
   * Register an action manually with explicit metadata.
   *
   * @param name Action name
   * @param metadata Action metadata
   * @throws IllegalArgumentException if action name already registered
   */
  public void registerAction(String name, ActionMetadata metadata) {
    Objects.requireNonNull(name, "Action name cannot be null");
    Objects.requireNonNull(metadata, "Action metadata cannot be null");

    if (actions.containsKey(name)) {
      throw new IllegalArgumentException(String.format("Action '%s' is already registered", name));
    }

    actions.put(name, metadata);
  }

  /**
   * Get action metadata by name.
   *
   * @param name Action name
   * @return Action metadata
   * @throws ActionNotFoundException if action not registered
   */
  public ActionMetadata getAction(String name) {
    Objects.requireNonNull(name, "Action name cannot be null");

    ActionMetadata metadata = actions.get(name);
    if (metadata == null) {
      throw new ActionNotFoundException(name);
    }

    return metadata;
  }

  /**
   * Check if action is registered.
   *
   * @param name Action name
   * @return true if action exists
   */
  public boolean hasAction(String name) {
    return actions.containsKey(name);
  }

  /**
   * Remove action from registry.
   *
   * @param name Action name
   * @return true if action was removed
   */
  public boolean removeAction(String name) {
    Objects.requireNonNull(name, "Action name cannot be null");
    return actions.remove(name) != null;
  }

  /** Clear all registered actions. */
  public void clear() {
    actions.clear();
  }

  /**
   * Get count of registered actions.
   *
   * @return Number of actions
   */
  public int size() {
    return actions.size();
  }

  /**
   * Create a Rule.Action wrapper for the given action name and arguments.
   *
   * <p>This wraps the action invocation to match the Rule.Action interface (Facts -> Facts).
   *
   * @param name Action name
   * @param args Arguments to pass to action (excluding Facts if present)
   * @return Rule.Action wrapper
   * @throws ActionNotFoundException if action not registered
   */
  public Rule.Action createAction(String name, Object... args) {
    ActionMetadata metadata = getAction(name);

    return facts -> {
      try {
        // Build full arguments array
        Object[] fullArgs;
        if (metadata.hasFactsParameter()) {
          fullArgs = new Object[args.length + 1];
          fullArgs[0] = facts;
          System.arraycopy(args, 0, fullArgs, 1, args.length);
        } else {
          fullArgs = args;
        }

        // Invoke action - validation done at registration
        Object result = metadata.getSpreader().invoke(fullArgs);

        // Return Facts: either from result or unchanged
        if (metadata.returnsVoid()) {
          return facts;
        } else if (result instanceof Facts) {
          return (Facts) result;
        } else {
          return facts;
        }

      } catch (Throwable t) {
        throw new RuntimeException(
            String.format("Error invoking action '%s': %s", name, t.getMessage()), t);
      }
    };
  }

  /**
   * Invoke an action directly with given arguments.
   *
   * @param name Action name
   * @param facts Current facts
   * @param args Additional arguments to pass to action
   * @return Updated facts
   * @throws ActionNotFoundException if action not registered
   * @throws IllegalArgumentException if argument count/types mismatch
   */
  public Facts invoke(String name, Facts facts, Object... args) {
    Rule.Action action = createAction(name, args);
    return action.execute(facts);
  }

  private void validateActionName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Action name cannot be null or empty");
    }
  }
}
