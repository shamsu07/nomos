package io.github.shamsu07.nomos.core.loader;

import io.github.shamsu07.nomos.core.action.ActionNotFoundException;
import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.expression.ExpressionEvaluator;
import io.github.shamsu07.nomos.core.expression.ParseException;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.rule.Rule;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.yaml.snakeyaml.Yaml;

/**
 * Loads rules from YAML files and converts them to Rule objects.
 *
 * <p>Thread-safe. Validates all functions and actions at load time (fail-fast).
 *
 * <p>Supports two action syntaxes:
 *
 * <ul>
 *   <li>Assignment: {@code discount.percent = 10}
 *   <li>Function call: {@code sendEmail(user.email, "message")}
 * </ul>
 */
public final class YAMLRuleLoader {

  private final ExpressionEvaluator evaluator;
  private final FunctionRegistry functionRegistry;
  private final ActionRegistry actionRegistry;

  public YAMLRuleLoader(
      ExpressionEvaluator evaluator,
      FunctionRegistry functionRegistry,
      ActionRegistry actionRegistry) {
    this.evaluator = Objects.requireNonNull(evaluator, "ExpressionEvaluator cannot be null");
    this.functionRegistry =
        Objects.requireNonNull(functionRegistry, "FunctionRegistry cannot be null");
    this.actionRegistry = Objects.requireNonNull(actionRegistry, "ActionRegistry cannot be null");
  }

  /**
   * Load rules from YAML input stream
   *
   * @param inputStream YAML input stream
   * @return List of parsed rules
   * @throws RuleParseException if parsing fails
   */
  public List<Rule> load(InputStream inputStream) {
    Objects.requireNonNull(inputStream, "InputStream cannot be null");

    try {
      Yaml yaml = new Yaml();
      Map<String, Object> data = yaml.load(inputStream);

      if (data == null || !data.containsKey("rules")) {
        throw new RuleParseException("YAML must contains 'rules' key");
      }

      List<Map<String, Object>> rulesData = (List<Map<String, Object>>) data.get("rules");

      if (rulesData == null || rulesData.isEmpty()) {
        return List.of();
      }

      List<Rule> rules = new ArrayList<>(rulesData.size());
      int lineNumber = 1;

      for (Map<String, Object> ruleData : rulesData) {
        rules.add(parseRule(ruleData, lineNumber));
        lineNumber++;
      }

      return rules;
    } catch (RuleParseException e) {
      throw e;
    } catch (Exception e) {
      throw new RuleParseException("Failed to load YAML rules", e);
    }
  }

  private Rule parseRule(Map<String, Object> ruleData, int lineNumber) {
    // Extract name
    String name = (String) ruleData.get("name");
    if (name == null || name.trim().isEmpty()) {
      throw new RuleParseException("Rule name is required", null, lineNumber);
    }

    try {
      // Extract priority (default 0)
      int priority = 0;
      if (ruleData.containsKey("priority")) {
        Object priorityObj = ruleData.get("priority");
        if (priorityObj instanceof Number) {
          priority = ((Number) priorityObj).intValue();
        } else {
          throw new RuleParseException("Priority must be a number", name, lineNumber);
        }
      }

      // Extract and parse condition
      String whenExpression = (String) ruleData.get("when");
      if (whenExpression == null || whenExpression.trim().isEmpty()) {
        throw new RuleParseException("Rule 'when' condition is required", name, lineNumber);
      }

      // Validate expression syntax
      try {
        evaluator.parse(whenExpression);
      } catch (ParseException e) {
        throw new RuleParseException(
            "Invalid 'when' expression: " + e.getMessage(), name, lineNumber, e);
      }

      // Extract actions
      @SuppressWarnings("unchecked")
      List<String> thenActions = (List<String>) ruleData.get("then");
      if (thenActions == null || thenActions.isEmpty()) {
        throw new RuleParseException("Rule must have at least one 'then' action", name, lineNumber);
      }

      // Build rule
      Rule.Builder builder =
          Rule.builder()
              .name(name)
              .priority(priority)
              .when(whenExpression)
              .when(facts -> (Boolean) evaluator.evaluate(whenExpression, facts));

      // Parse and add actions
      for (String actionString : thenActions) {
        Rule.Action action = parseAction(actionString, name, lineNumber);
        builder.then(action);
      }

      return builder.build();
    } catch (RuleParseException e) {
      throw e;
    } catch (Exception e) {
      throw new RuleParseException("Failed to parse rule", name, lineNumber, e);
    }
  }

  private Rule.Action parseAction(String actionString, String ruleName, int lineNumber) {
    if (actionString == null || actionString.trim().isEmpty()) {
      throw new RuleParseException("Action string cannot be empty", ruleName, lineNumber);
    }

    actionString = actionString.trim();

    // Check if it's an assignment (contains '=' but not inside parenthesis)
    int equalsIndex = findAssignmentEquals(actionString);

    if (equalsIndex > 0) {
      // Assignment: key = value
      return parseAssignment(actionString, equalsIndex, ruleName, lineNumber);
    } else {
      // Function call: functionName(args)
      return parseFunctionCall(actionString, ruleName, lineNumber);
    }
  }

  private int findAssignmentEquals(String str) {
    int depth = 0;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c == '(') {
        depth++;
      } else if (c == ')') {
        depth--;
      } else if (c == '=' && depth == 0) {
        return i;
      }
    }
    return -1;
  }

  private Rule.Action parseAssignment(
      String actionString, int equalsIndex, String ruleName, int lineNumber) {
    String key = actionString.substring(0, equalsIndex).trim();
    String valueExpression = actionString.substring(equalsIndex + 1).trim();

    if (key.isEmpty()) {
      throw new RuleParseException("Assignment key cannot be empty", ruleName, lineNumber);
    }

    if (valueExpression.isEmpty()) {
      throw new RuleParseException("Assignment value cannot be empty", ruleName, lineNumber);
    }

    // Validate value expression syntax
    try {
      evaluator.parse(valueExpression);
    } catch (ParseException e) {
      throw new RuleParseException(
          " Invalid assignment value expression" + e.getMessage(), ruleName, lineNumber, e);
    }

    return facts -> {
      Object value = evaluator.evaluate(valueExpression, facts);
      return facts.put(key, value);
    };
  }

  private Rule.Action parseFunctionCall(String actionString, String ruleName, int lineNumber) {
    // Parse as expression to extract function name and arguments
    try {
      // Check iff it looks like a function call
      if (!actionString.contains("(") || !actionString.endsWith(")")) {
        throw new RuleParseException(
            "Invalid action syntax. Expected assignment 'key = value' or function call 'func(args)'",
            ruleName,
            lineNumber);
      }

      int openParen = actionString.indexOf("(");
      String functionName = actionString.substring(0, openParen).trim();

      // Validate function exists in action registry
      if (!actionRegistry.hasAction(functionName)) {
        throw new ActionNotFoundException(functionName, ruleName, lineNumber);
      }

      // Parse arguments by evaluating the full expression
      // We will extract arguments at runtime from the parsed expression
      return facts -> {
        // Parse the action string as an expression to get arguments
        String argsString = actionString.substring(openParen + 1, actionString.length() - 1).trim();

        if (argsString.isEmpty()) {
          return actionRegistry.invoke(functionName, facts);
        }

        // Split arguments by comma (respecting nested parentheses and quotes)
        List<Object> args = parseArguments(argsString, facts);

        return actionRegistry.invoke(functionName, facts, args.toArray());
      };
    } catch (ActionNotFoundException e) {
      throw e;
    } catch (RuleParseException e) {
      throw e;
    } catch (Exception e) {
      throw new RuleParseException(
          "Failed to parse action function call: " + actionString, ruleName, lineNumber, e);
    }
  }

  private List<Object> parseArguments(String argsString, Facts facts) {
    List<Object> arguments = new ArrayList<>();

    if (argsString.isEmpty()) {
      return arguments;
    }

    // Split by comma, respoecting nested parentheses, brackets, and quotes
    List<String> argTokens = splitArguments(argsString);

    for (String arg : argTokens) {
      arg = arg.trim();
      // Evaluate each argument as an expression
      Object value = evaluator.evaluate(arg, facts);
      arguments.add(value);
    }

    return arguments;
  }

  private List<String> splitArguments(String argsString) {
    List<String> arguments = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    int depth = 0;
    boolean inString = false;
    char stringChar = 0;

    for (int i = 0; i < argsString.length(); i++) {
      char c = argsString.charAt(i);

      if (inString) {
        current.append(c);
        if (c == stringChar && (i == 0 || argsString.charAt(i - 1) != '\\')) {
          inString = false;
        }
      } else {
        if (c == '"' || c == '\'') {
          inString = true;
          stringChar = c;
          current.append(c);
        } else if (c == '(' || c == '[') {
          depth++;
          current.append(c);
        } else if (c == ')' || c == ']') {
          depth--;
          current.append(c);
        } else if (c == ',' && depth == 0) {
          arguments.add(current.toString().trim());
          current.setLength(0);
        } else {
          current.append(c);
        }
      }
    }

    if (current.length() > 0) {
      arguments.add(current.toString().trim());
    }

    return arguments;
  }
}
