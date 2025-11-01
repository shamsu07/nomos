package io.github.shamsu07.nomos.core.rule;

import io.github.shamsu07.nomos.core.facts.Facts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Immutable rule definition containing conditions and actions. Rules are evaluated in priority
 * order (highes = first)
 */
public final class Rule {

  private final String name;
  private final int priority;
  private final String conditionExpression;
  private final Predicate<Facts> condition;
  private final List<Action> actions;

  private Rule(Builder builder) {
    this.name = builder.name;
    this.priority = builder.priority;
    this.conditionExpression = builder.conditionExpression;
    this.condition = builder.condition;
    this.actions = Collections.unmodifiableList(new ArrayList<>(builder.actions));
  }

  public String getName() {
    return name;
  }

  public int getPriority() {
    return priority;
  }

  public String getConditionExpression() {
    return conditionExpression;
  }

  /**
   * Evaluate rule condition against facts
   *
   * @return true if condition matches
   */
  public boolean evaluate(Facts facts) {
    Objects.requireNonNull(facts, "Facts cannot be null");
    if (condition != null) {
      return condition.test(facts);
    }
    return false;
  }

  /**
   * Execute all actions for this rule.
   *
   * @param facts Current facts
   * @return Updated facts after all actions
   */
  public Facts execute(Facts facts) {
    Objects.requireNonNull(facts, "Facts cannot be null");
    Facts current = facts;
    for (Action action : actions) {
      current = action.execute(current);
    }
    return current;
  }

  public List<Action> getActions() {
    return actions;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private int priority = 0;
    private String conditionExpression;
    private Predicate<Facts> condition;
    private List<Action> actions = new ArrayList<>();

    /** Set rule name (required) */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /** Set rule priority. Higher values execute first. Default: 0. */
    public Builder priority(int priority) {
      this.priority = priority;
      return this;
    }

    /** Set condition as string expression (for YAML rules). */
    public Builder when(String expression) {
      this.conditionExpression = expression;
      return this;
    }

    /** Set condition as lambda (for Java DSL). */
    public Builder when(Predicate<Facts> condition) {
      this.condition = condition;
      return this;
    }

    /** Add action to execute when rule fires. */
    public Builder then(Action action) {
      Objects.requireNonNull(action, "Action cannot be null");
      this.actions.add(action);
      return this;
    }

    public Rule build() {
      Objects.requireNonNull(name, "Rule name is required");
      if (conditionExpression == null && condition == null) {
        throw new IllegalStateException("Rule must have a condition (when)");
      }
      if (actions.isEmpty()) {
        throw new IllegalStateException("Rule must have at least one action (then)");
      }
      return new Rule(this);
    }
  }

  /** Functional interface for rule actions. */
  @FunctionalInterface
  public interface Action {
    /**
     * Execute action on facts
     *
     * @param facts Current facts
     * @return Updated facts
     */
    Facts execute(Facts facts);
  }
}
