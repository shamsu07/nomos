package io.github.shamsu07.nomos.core.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a reusable function callable from rule conditions
 *
 * <p>Functions must be pure (no side effects) and deterministic. They are invoked during rule
 * evaluation (when clauses)
 *
 * <p>Example:
 *
 * <pre>
 * {@code @NomosFunction("isVIP")}
 * boolean isVIP(User user) { return "VIP".equals(user.getTier()); }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NomosFunction {
  /**
   * Function name user in rule expressions. Must be unique across the registry.
   *
   * @return Function identifier
   */
  String value();
}
