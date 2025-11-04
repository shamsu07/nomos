package io.github.shamsu07.nomos.core.loader;

/**
 * Thrown when YAML rule parsing fails due to syntax errors, missing functions, or invalid
 * configuration.
 *
 * <p>Includes context like rule name and line information for debugging.
 */
public class RuleParseException extends RuntimeException {

  private final String ruleName;
  private final int lineNumber;

  public RuleParseException(String message) {
    super(message);
    this.ruleName = null;
    this.lineNumber = -1;
  }

  public RuleParseException(String message, Throwable cause) {
    super(message, cause);
    this.ruleName = null;
    this.lineNumber = -1;
  }

  public RuleParseException(String message, String ruleName) {
    super(formatMessage(message, ruleName, -1));
    this.ruleName = ruleName;
    this.lineNumber = -1;
  }

  public RuleParseException(String message, String ruleName, int lineNumber) {
    super(formatMessage(message, ruleName, lineNumber));
    this.ruleName = ruleName;
    this.lineNumber = lineNumber;
  }

  public RuleParseException(String message, String ruleName, Throwable cause) {
    super(formatMessage(message, ruleName, -1), cause);
    this.ruleName = ruleName;
    this.lineNumber = -1;
  }

  public RuleParseException(String message, String ruleName, int lineNumber, Throwable cause) {
    super(formatMessage(message, ruleName, lineNumber), cause);
    this.ruleName = ruleName;
    this.lineNumber = lineNumber;
  }

  private static String formatMessage(String message, String ruleName, int lineNumber) {
    if (ruleName == null) {
      return message;
    }
    if (lineNumber > 0) {
      return String.format("%s in rule '%s' (line %d)", message, ruleName, lineNumber);
    }
    return String.format("%s in rule '%s'", message, ruleName);
  }

  public String getRuleName() {
    return ruleName;
  }

  public int getLineNumber() {
    return lineNumber;
  }
}
