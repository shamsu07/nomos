package io.github.shamsu07.nomos.core.engine;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Forward-chaining rule execution engine. Thread-safe for concurrent reads and rule modifications.
 */
public final class RuleEngine {

  private final List<Rule> rules;
  private final Object rulesLock = new Object(); // Lock for rule modifications
  private final boolean stopOnFirstAppliedRule;

  public RuleEngine() {
    this(false);
  }

  /**
   * Create engine with execution strategy
   *
   * @param stopOnFirstAppliedRule If true, stop after first matching rule fires
   */
  public RuleEngine(boolean stopOnFirstAppliedRule) {
    this.rules = new CopyOnWriteArrayList<>();
    this.stopOnFirstAppliedRule = stopOnFirstAppliedRule;
  }

  /**
   * Add rule to engine. Rules are added and then sorted to maintain priority order (highest first).
   * Thread-safe: uses lock to ensure atomic clear+addAll operation.
   *
   * @param rule Rule to add
   * @throws NullPointerException if rule is null
   */
  public void addRule(Rule rule) {
    Objects.requireNonNull(rule, "Rule cannot be null");

    synchronized (rulesLock) {
      // Build a new sorted list with all rules including the new one
      List<Rule> newRules = new ArrayList<>(rules);
      newRules.add(rule);
      // Sort by priority descending (highest first)
      newRules.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));

      // Replace the entire list content atomically within lock
      // This prevents execute() from seeing an empty list during the transition
      rules.clear();
      rules.addAll(newRules);
    }
  }

  /**
   * Remove rule by name.
   *
   * @param name Rule name
   * @return true if rule was removed
   */
  public boolean removeRule(String name) {
    Objects.requireNonNull(name, "Rule name cannot be null");
    return rules.removeIf(rule -> rule.getName().equals(name));
  }

  /** Remove all rules. */
  public void clearRules() {
    rules.clear();
  }

  /** Get all registered rules (unmodifiable view). */
  public List<Rule> getRules() {
    return new ArrayList<>(rules);
  }

  /**
   * Execute all matching rules against facts. Rules fire in priority order(highest first). Rules
   * are already maintained in sorted order, so no sorting is needed.
   *
   * @param facts Input facts
   * @return Updated facts after rule execution
   * @throws NullPointerException if facts is null
   */
  public Facts execute(Facts facts) {
    Objects.requireNonNull(facts, "Facts cannot be null");

    Facts current = facts;

    // Iterate over rules. CopyOnWriteArrayList provides a consistent snapshot,
    // but we need to ensure we don't read during clear+addAll window
    synchronized (rulesLock) {
      for (Rule rule : rules) {
        if (rule.evaluate(current)) {
          current = rule.execute(current);
          if (stopOnFirstAppliedRule) {
            break;
          }
        }
      }
    }

    return current;
  }

  /**
   * Execute rules and collect execution trace. Rules are already maintained in sorted order.
   *
   * @param facts Input facts
   * @return Execution result with trace
   */
  public ExecutionResult executeWithTrace(Facts facts) {
    Objects.requireNonNull(facts, "Facts cannot be null");

    Facts current = facts;
    List<String> firedRules = new ArrayList<>();

    // Iterate over rules with lock to ensure consistent snapshot
    synchronized (rulesLock) {
      for (Rule rule : rules) {
        if (rule.evaluate(current)) {
          firedRules.add(rule.getName());
          current = rule.execute(current);
          if (stopOnFirstAppliedRule) {
            break;
          }
        }
      }
    }

    return new ExecutionResult(current, firedRules);
  }

  /** Result of rule execution with trace information. */
  public static final class ExecutionResult {
    private final Facts facts;
    private final List<String> firedRules;

    public ExecutionResult(Facts facts, List<String> firedRules) {
      this.facts = facts;
      this.firedRules = List.copyOf(firedRules);
    }

    public Facts getFacts() {
      return facts;
    }

    public List<String> getFiredRules() {
      return firedRules;
    }

    @Override
    public String toString() {
      return String.format("ExecutionResult[firedRules=%s]", firedRules);
    }
  }
}
