package io.github.shamsu07.nomos.core.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LexerTest {

  @Test
  void should_tokenizeNumber_when_integer() {
    List<Token> tokens = new Lexer("42").tokenize();
    assertEquals(2, tokens.size());
    assertEquals(TokenType.NUMBER, tokens.get(0).getType());
    assertEquals(42.0, tokens.get(0).getLiteral());
  }

  @Test
  void should_tokenizeNumber_when_decimal() {
    List<Token> tokens = new Lexer("3.14").tokenize();
    assertEquals(TokenType.NUMBER, tokens.get(0).getType());
    assertEquals(3.14, tokens.get(0).getLiteral());
  }

  @Test
  void should_tokenizeString_when_doubleQuotes() {
    List<Token> tokens = new Lexer("\"hello\"").tokenize();
    assertEquals(TokenType.STRING, tokens.get(0).getType());
    assertEquals("hello", tokens.get(0).getLiteral());
  }

  @Test
  void should_tokenizeString_when_singleQuotes() {
    List<Token> tokens = new Lexer("'world'").tokenize();
    assertEquals(TokenType.STRING, tokens.get(0).getType());
    assertEquals("world", tokens.get(0).getLiteral());
  }

  @Test
  void should_tokenizeBooleans_when_trueOrFalse() {
    List<Token> tokens = new Lexer("true false").tokenize();
    assertEquals(TokenType.TRUE, tokens.get(0).getType());
    assertEquals(true, tokens.get(0).getLiteral());
    assertEquals(TokenType.FALSE, tokens.get(1).getType());
    assertEquals(false, tokens.get(1).getLiteral());
  }

  @Test
  void should_tokenizeNull_when_null() {
    List<Token> tokens = new Lexer("null").tokenize();
    assertEquals(TokenType.NULL, tokens.get(0).getType());
    assertEquals(null, tokens.get(0).getLiteral());
  }

  @Test
  void should_tokenizeIdentifier_when_alphanumeric() {
    List<Token> tokens = new Lexer("user123").tokenize();
    assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
    assertEquals("user123", tokens.get(0).getLexeme());
  }

  @Test
  void should_tokenizeLogicalOperators_when_andOrNot() {
    List<Token> tokens = new Lexer("&& || !").tokenize();
    assertEquals(TokenType.AND, tokens.get(0).getType());
    assertEquals(TokenType.OR, tokens.get(1).getType());
    assertEquals(TokenType.NOT, tokens.get(2).getType());
  }

  @Test
  void should_tokenizeComparisonOperators_when_present() {
    List<Token> tokens = new Lexer("== != < > <= >=").tokenize();
    assertEquals(TokenType.EQUAL, tokens.get(0).getType());
    assertEquals(TokenType.NOT_EQUAL, tokens.get(1).getType());
    assertEquals(TokenType.LESS, tokens.get(2).getType());
    assertEquals(TokenType.GREATER, tokens.get(3).getType());
    assertEquals(TokenType.LESS_EQUAL, tokens.get(4).getType());
    assertEquals(TokenType.GREATER_EQUAL, tokens.get(5).getType());
  }

  @Test
  void should_tokenizeArithmeticOperators_when_present() {
    List<Token> tokens = new Lexer("+ - * /").tokenize();
    assertEquals(TokenType.PLUS, tokens.get(0).getType());
    assertEquals(TokenType.MINUS, tokens.get(1).getType());
    assertEquals(TokenType.MULTIPLY, tokens.get(2).getType());
    assertEquals(TokenType.DIVIDE, tokens.get(3).getType());
  }

  @Test
  void should_tokenizePunctuation_when_parensCommasDots() {
    List<Token> tokens = new Lexer("( ) , .").tokenize();
    assertEquals(TokenType.LEFT_PAREN, tokens.get(0).getType());
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(1).getType());
    assertEquals(TokenType.COMMA, tokens.get(2).getType());
    assertEquals(TokenType.DOT, tokens.get(3).getType());
  }

  @Test
  void should_ignoreWhitespace_when_tokenizing() {
    List<Token> tokens = new Lexer("  a  \t b  \n c  ").tokenize();
    assertEquals(4, tokens.size()); // 3 identifiers + EOF
    assertEquals("a", tokens.get(0).getLexeme());
    assertEquals("b", tokens.get(1).getLexeme());
    assertEquals("c", tokens.get(2).getLexeme());
  }

  @Test
  void should_tokenizeComplexExpression_when_mixed() {
    List<Token> tokens = new Lexer("isVIP(user) && cart.total > 100").tokenize();

    assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType()); // isVIP
    assertEquals(TokenType.LEFT_PAREN, tokens.get(1).getType());
    assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType()); // user
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(3).getType());
    assertEquals(TokenType.AND, tokens.get(4).getType());
    assertEquals(TokenType.IDENTIFIER, tokens.get(5).getType()); // cart
    assertEquals(TokenType.DOT, tokens.get(6).getType());
    assertEquals(TokenType.IDENTIFIER, tokens.get(7).getType()); // total
    assertEquals(TokenType.GREATER, tokens.get(8).getType());
    assertEquals(TokenType.NUMBER, tokens.get(9).getType()); // 100
    assertEquals(TokenType.EOF, tokens.get(10).getType());
  }

  @Test
  void should_throwException_when_unterminatedDoubleQuoteString() {
    assertThrows(ParseException.class, () -> new Lexer("\"unterminated").tokenize());
  }

  @Test
  void should_throwException_when_unterminatedSingleQuoteString() {
    assertThrows(ParseException.class, () -> new Lexer("'unterminated").tokenize());
  }

  @Test
  void should_throwException_when_unexpectedCharacter() {
    assertThrows(ParseException.class, () -> new Lexer("@").tokenize());
  }

  @Test
  void should_throwException_when_singleAmpersand() {
    assertThrows(ParseException.class, () -> new Lexer("a & b").tokenize());
  }

  @Test
  void should_throwException_when_singlePipe() {
    assertThrows(ParseException.class, () -> new Lexer("a | b").tokenize());
  }

  @Test
  void should_throwException_when_singleEquals() {
    assertThrows(ParseException.class, () -> new Lexer("a = b").tokenize());
  }

  @Test
  void should_endWithEOF_when_tokenizingAnyInput() {
    List<Token> tokens = new Lexer("test").tokenize();
    assertEquals(TokenType.EOF, tokens.get(tokens.size() - 1).getType());
  }

  @Test
  void should_tokenizeEmptyString_when_onlyWhitespace() {
    List<Token> tokens = new Lexer("   ").tokenize();
    assertEquals(1, tokens.size());
    assertEquals(TokenType.EOF, tokens.get(0).getType());
  }

  @Test
  void should_tokenizeDotNotation_when_nestedProperties() {
    List<Token> tokens = new Lexer("user.address.city").tokenize();
    assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
    assertEquals("user", tokens.get(0).getLexeme());
    assertEquals(TokenType.DOT, tokens.get(1).getType());
    assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
    assertEquals("address", tokens.get(2).getLexeme());
    assertEquals(TokenType.DOT, tokens.get(3).getType());
    assertEquals(TokenType.IDENTIFIER, tokens.get(4).getType());
    assertEquals("city", tokens.get(4).getLexeme());
  }

  @Test
  void should_tokenizeNegativeNumber_when_minusBeforeNumber() {
    List<Token> tokens = new Lexer("-42").tokenize();
    assertEquals(TokenType.MINUS, tokens.get(0).getType());
    assertEquals(TokenType.NUMBER, tokens.get(1).getType());
    assertEquals(42.0, tokens.get(1).getLiteral());
  }

  @Test
  void should_tokenizeFunctionCall_when_parensAfterIdentifier() {
    List<Token> tokens = new Lexer("func(a, b)").tokenize();
    assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
    assertEquals("func", tokens.get(0).getLexeme());
    assertEquals(TokenType.LEFT_PAREN, tokens.get(1).getType());
    assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
    assertEquals(TokenType.COMMA, tokens.get(3).getType());
    assertEquals(TokenType.IDENTIFIER, tokens.get(4).getType());
    assertEquals(TokenType.RIGHT_PAREN, tokens.get(5).getType());
  }

  @Test
  void should_parseEscapedQuote_when_doubleQuoteString() {
    List<Token> tokens = new Lexer("\"He said \\\"Hello\\\"\"").tokenize();
    assertEquals(TokenType.STRING, tokens.get(0).getType());
    assertEquals("He said \"Hello\"", tokens.get(0).getLiteral());
  }

  @Test
  void should_parseEscapedQuote_when_singleQuoteString() {
    List<Token> tokens = new Lexer("'It\\'s working'").tokenize();
    assertEquals(TokenType.STRING, tokens.get(0).getType());
    assertEquals("It's working", tokens.get(0).getLiteral());
  }

  @Test
  void should_parseEscapedBackslash_when_present() {
    List<Token> tokens = new Lexer("\"path\\\\to\\\\file\"").tokenize();
    assertEquals(TokenType.STRING, tokens.get(0).getType());
    assertEquals("path\\to\\file", tokens.get(0).getLiteral());
  }

  @Test
  void should_parseNewlineEscape_when_backslashN() {
    List<Token> tokens = new Lexer("\"line1\\nline2\"").tokenize();
    assertEquals(TokenType.STRING, tokens.get(0).getType());
    assertEquals("line1\nline2", tokens.get(0).getLiteral());
  }

  @Test
  void should_parseTabEscape_when_backslashT() {
    List<Token> tokens = new Lexer("\"col1\\tcol2\"").tokenize();
    assertEquals(TokenType.STRING, tokens.get(0).getType());
    assertEquals("col1\tcol2", tokens.get(0).getLiteral());
  }

  @Test
  void should_parseCarriageReturnEscape_when_backslashR() {
    List<Token> tokens = new Lexer("\"text\\rmore\"").tokenize();
    assertEquals(TokenType.STRING, tokens.get(0).getType());
    assertEquals("text\rmore", tokens.get(0).getLiteral());
  }

  @Test
  void should_throwException_when_invalidEscapeSequence() {
    ParseException ex =
        assertThrows(ParseException.class, () -> new Lexer("\"invalid\\x\"").tokenize());
    // Message includes position info, just verify the key part
    assertTrue(ex.getMessage().contains("Invalid escape sequence: \\x"));
  }

  @Test
  void should_throwException_when_unterminatedEscapeSequence() {
    assertThrows(ParseException.class, () -> new Lexer("\"trailing\\").tokenize());
  }
}
