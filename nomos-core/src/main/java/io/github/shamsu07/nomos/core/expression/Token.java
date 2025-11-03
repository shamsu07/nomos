package io.github.shamsu07.nomos.core.expression;

import java.util.Objects;

/**
 * Represents a token in an expression.
 *
 * <p>Immutable value object containing token type, lexeme, and literal value.
 */
public final class Token {

  private final TokenType type;
  private final String lexeme;
  private final Object literal;
  private final int position;

  public Token(TokenType type, String lexeme, Object literal, int position) {
    this.type = Objects.requireNonNull(type, "Token type cannot be null");
    this.lexeme = Objects.requireNonNull(lexeme, "Lexeme cannot be null");
    this.literal = literal;
    this.position = position;
  }

  public TokenType getType() {
    return type;
  }

  public String getLexeme() {
    return lexeme;
  }

  public Object getLiteral() {
    return literal;
  }

  public int getPosition() {
    return position;
  }

  @Override
  public String toString() {
    return String.format("Token[%s, '%s', %s, pos=%d]", type, lexeme, literal, position);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Token)) {
      return false;
    }
    Token other = (Token) obj;
    return type == other.type
        && lexeme.equals(other.lexeme)
        && Objects.equals(literal, other.literal)
        && position == other.position;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, lexeme, literal, position);
  }
}
