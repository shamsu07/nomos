package io.github.shamsu07.nomos.core.expression;

/**
 * Thrown when expression parsing fails due to syntax errors.
 *
 * <p>Includes position information for debugging.
 */
public class ParseException extends RuntimeException {

  private final int position;

  public ParseException(String message, int position) {
    super(String.format("%s (at position %d)", message, position));
    this.position = position;
  }

  public ParseException(String message, int position, Throwable cause) {
    super(String.format("%s (at position %d)", message, position), cause);
    this.position = position;
  }

  public int getPosition() {
    return position;
  }
}
