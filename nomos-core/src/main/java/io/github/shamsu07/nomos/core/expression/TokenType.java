package io.github.shamsu07.nomos.core.expression;

/**
 * Token types for expression parsing.
 *
 * <p>Supports logical operators, comparison operators, arithmetic operators, literals, and function
 * calls.
 */
public enum TokenType {
  // Literals
  NUMBER,
  STRING,
  TRUE,
  FALSE,
  NULL,

  // Identifier and keywords
  IDENTIFIER,

  // Operators - Logical
  AND, // &&
  OR, // ||
  NOT, // !

  // Operator - Comparison
  EQUAL, // ==
  NOT_EQUAL, // !=
  LESS, // <
  GREATER, // >
  LESS_EQUAL, // <=
  GREATER_EQUAL, // >=

  // Operators - Arithmetic
  PLUS, // +
  MINUS, // -
  MULTIPLY, // *
  DIVIDE, // /
  MODULO, // %

  // Punctuation
  LEFT_PAREN, // (
  RIGHT_PAREN, // )
  COMMA, // ,
  DOT, // .

  // Special
  EOF
}
