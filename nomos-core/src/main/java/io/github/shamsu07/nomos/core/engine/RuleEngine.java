package io.github.shamsu07.nomos.core.engine;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Forward-chaining rule execution engine. Thread-safe for concurrent reads and rule modifications.
 */
public final class RuleEngine {

  private final List<Rule> rules;
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
   * Add rule to engine
   *
   * @param rule Rule to add
   * @throws NullPointerException if rule is null
   */
  public void addRule(Rule rule) {
    Objects.requireNonNull(rule, "Rule cannot be null");
    rules.add(rule);
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
   * Execute all matching rules against facts. Rules fire in priority order(highest first).
   *
   * @param facts Input facts
   * @return Updated facts after rule execution
   * @throws NullPointerException if facts is null
   */
  public Facts execute(Facts facts) {
    Objects.requireNonNull(facts, "Facts cannot be null");

    List<Rule> sortedRules = getSortedRules();
    Facts current = facts;

    for (Rule rule : sortedRules) {
      if (rule.evaluate(current)) {
        current = rule.execute(current);
        if (stopOnFirstAppliedRule) {
          break;
        }
      }
    }

    return current;
  }

  /**
   * Execute rules and collect execution trace
   *
   * @param facts Input facts
   * @return Execution result with trace
   */
  public ExecutionResult executeWithTrace(Facts facts) {
    Objects.requireNonNull(facts, "Facts cannot be null");

    List<Rule> sortedRules = getSortedRules();
    Facts current = facts;
    List<String> firedRules = new ArrayList<>();

    for (Rule rule : sortedRules) {
      if (rule.evaluate(current)) {
        firedRules.add(rule.getName());
        current = rule.execute(current);
        if (stopOnFirstAppliedRule) {
          break;
        }
      }
    }

    return new ExecutionResult(current, firedRules);
  }

  private List<Rule> getSortedRules() {
    List<Rule> sorted = new ArrayList<>(rules);
    sorted.sort(Comparator.comparingInt(Rule::getPriority).reversed());
    return sorted;
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
