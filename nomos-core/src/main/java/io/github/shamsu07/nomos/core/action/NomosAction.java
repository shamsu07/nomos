package io.github.shamsu07.nomos.core.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a reusable action callable from rule then clauses.
 *
 * <p>Actions can modify facts or perform side effects. They may return void or Facts.
 *
 * <p>Example:
 *
 * <pre>
 * {@code @NomosAction("sendEmail")}
 * void sendEmail(String email, String message) { emailService.send(email, message); }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NomosAction {
  /**
   * Action name used in rule then clauses. Must be uniqu across the registry.
   *
   * @return Action identifier
   */
  String value();
}
