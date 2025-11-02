package io.github.shamsu07.nomos.core.action;

/**
 * Thrown when an action referenced in a rule then clause is not registered in the action registry.
 */
public class ActionNotFoundException extends RuntimeException {

  private final String actionName;

  public ActionNotFoundException(String actionName) {
    super(String.format("Action '%s' is not registered", actionName));
    this.actionName = actionName;
  }

  public ActionNotFoundException(String actionName, String ruleName, int line) {
    super(
        String.format("Action '%s' not found in rule '%s' (line %d)", actionName, ruleName, line));
    this.actionName = actionName;
  }

  public String getActionName() {
    return actionName;
  }
}
