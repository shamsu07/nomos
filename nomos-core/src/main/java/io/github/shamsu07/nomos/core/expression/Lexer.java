package io.github.shamsu07.nomos.core.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexical analyzer for expressions.
 *
 * <p>Converts input string into sequence of tokens for parsing.
 */
public final class Lexer {

  private final String source;
  private final List<Token> tokens;
  private int start;
  private int current;

  public Lexer(String source) {
    this.source = source;
    this.tokens = new ArrayList<>();
    this.start = 0;
    this.current = 0;
  }

  /**
   * Tokenize the source string.
   *
   * @return List of tokens
   * @throws ParseException if invalid syntax encountered
   */
  public List<Token> tokenize() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, current));
    return tokens;
  }

  private void scanToken() {
    char c = advance();

    switch (c) {
      case ' ':
      case '\r':
      case '\t':
      case '\n':
        // Ignore whitespace
        break;
      case '(':
        addToken(TokenType.LEFT_PAREN);
        break;
      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;
      case ',':
        addToken(TokenType.COMMA);
        break;
      case '.':
        addToken(TokenType.DOT);
        break;
      case '+':
        addToken(TokenType.PLUS);
        break;
      case '-':
        addToken(TokenType.MINUS);
        break;
      case '*':
        addToken(TokenType.MULTIPLY);
        break;
      case '/':
        addToken(TokenType.DIVIDE);
        break;
      case '!':
        addToken(match('=') ? TokenType.NOT_EQUAL : TokenType.NOT);
        break;
      case '=':
        if (match('=')) {
          addToken(TokenType.EQUAL);
        } else {
          throw new ParseException("Unexpected character '='", start);
        }
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case '&':
        if (match('&')) {
          addToken(TokenType.AND);
        } else {
          throw new ParseException("Unexpected character '&'", start);
        }
        break;
      case '|':
        if (match('|')) {
          addToken(TokenType.OR);
        } else {
          throw new ParseException("Unexpected character '|'", start);
        }
        break;
      case '"':
        string();
        break;
      case '\'':
        singleQuoteString();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          throw new ParseException("Unexpected character '" + c + "'", start);
        }
    }
  }

  private void string() {
    StringBuilder value = new StringBuilder();

    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\\') {
        advance(); // consume backslash
        value.append(processEscapeSequence());
      } else {
        value.append(advance());
      }
    }

    if (isAtEnd()) {
      throw new ParseException("Unterminated string", start);
    }

    // Closing "
    advance();

    addToken(TokenType.STRING, value.toString());
  }

  private void singleQuoteString() {
    StringBuilder value = new StringBuilder();

    while (peek() != '\'' && !isAtEnd()) {
      if (peek() == '\\') {
        advance(); // consume backslash
        value.append(processEscapeSequence());
      } else {
        value.append(advance());
      }
    }

    if (isAtEnd()) {
      throw new ParseException("Unterminated string", start);
    }

    // Closing '
    advance();

    addToken(TokenType.STRING, value.toString());
  }

  /**
   * Process escape sequence after backslash has been consumed.
   *
   * @return The character represented by the escape sequence
   */
  private char processEscapeSequence() {
    if (isAtEnd()) {
      throw new ParseException("Unterminated escape sequence", current - 1);
    }

    char escaped = advance();
    switch (escaped) {
      case '"':
        return '"';
      case '\'':
        return '\'';
      case '\\':
        return '\\';
      case 'n':
        return '\n';
      case 't':
        return '\t';
      case 'r':
        return '\r';
      default:
        throw new ParseException("Invalid escape sequence: \\" + escaped, current - 2);
    }
  }

  private void number() {
    while (isDigit(peek())) {
      advance();
    }

    // Look for decimal
    if (peek() == '.' && isDigit(peekNext())) {
      advance(); // Consume '.'
      while (isDigit(peek())) {
        advance();
      }
    }

    String text = source.substring(start, current);
    addToken(TokenType.NUMBER, Double.parseDouble(text));
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) {
      advance();
    }
    String text = source.substring(start, current);
    TokenType type;

    // Check for keywords
    switch (text) {
      case "true":
        type = TokenType.TRUE;
        addToken(type, true);
        return;
      case "false":
        type = TokenType.FALSE;
        addToken(type, false);
        return;
      case "null":
        type = TokenType.NULL;
        addToken(type, null);
        return;
      default:
        type = TokenType.IDENTIFIER;
        addToken(type);
    }
  }

  private boolean match(char expected) {
    if (isAtEnd()) {
      return false;
    }
    if (source.charAt(current) != expected) {
      return false;
    }
    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) {
      return '\0';
    }
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) {
      return '\0';
    }
    return source.charAt(current + 1);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, current));
  }
}
