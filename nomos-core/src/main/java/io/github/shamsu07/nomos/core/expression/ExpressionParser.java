package io.github.shamsu07.nomos.core.expression;

import io.github.shamsu07.nomos.core.expression.ast.BinaryExpression;
import io.github.shamsu07.nomos.core.expression.ast.Expression;
import io.github.shamsu07.nomos.core.expression.ast.FunctionCallExpression;
import io.github.shamsu07.nomos.core.expression.ast.LiteralExpression;
import io.github.shamsu07.nomos.core.expression.ast.UnaryExpression;
import io.github.shamsu07.nomos.core.expression.ast.VariableExpression;
import java.util.ArrayList;
import java.util.List;

/**
 * Recursive descent parser for expressions.
 *
 * <p>Converts token stream into AST following standard operator precedence:
 *
 * <pre>
 * expression → or
 * or         → and ( "||" and )*
 * and        → equality ( "&&" equality )*
 * equality   → comparison ( ( "==" | "!=" ) comparison )*
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )*
 * term       → factor ( ( "+" | "-" ) factor )*
 * factor     → unary ( ( "*" | "/" ) unary )*
 * unary      → ( "!" | "-" ) unary | primary
 * primary    → literal | identifier | functionCall | "(" expression ")"
 * </pre>
 */
public final class ExpressionParser {

  private final List<Token> tokens;
  private int current;

  public ExpressionParser(List<Token> tokens) {
    this.tokens = tokens;
    this.current = 0;
  }

  /**
   * Parse token stream into AST.
   *
   * @return Root expression node
   * @throws ParseException if syntax error encountered
   */
  public Expression parse() {
    try {
      return expression();
    } catch (Exception e) {
      throw new ParseException("Failed to parse expression: " + e.getMessage(), current, e);
    }
  }

  private Expression expression() {
    return or();
  }

  private Expression or() {
    Expression expr = and();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expression right = and();
      expr = new BinaryExpression(expr, operator.getType(), right);
    }

    return expr;
  }

  private Expression and() {
    Expression expr = equality();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expression right = equality();
      expr = new BinaryExpression(expr, operator.getType(), right);
    }

    return expr;
  }

  private Expression equality() {
    Expression expr = comparison();

    while (match(TokenType.EQUAL, TokenType.NOT_EQUAL)) {
      Token operator = previous();
      Expression right = comparison();
      expr = new BinaryExpression(expr, operator.getType(), right);
    }

    return expr;
  }

  private Expression comparison() {
    Expression expr = term();

    while (match(
        TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token operator = previous();
      Expression right = term();
      expr = new BinaryExpression(expr, operator.getType(), right);
    }

    return expr;
  }

  private Expression term() {
    Expression expr = factor();

    while (match(TokenType.PLUS, TokenType.MINUS)) {
      Token operator = previous();
      Expression right = factor();
      expr = new BinaryExpression(expr, operator.getType(), right);
    }

    return expr;
  }

  private Expression factor() {
    Expression expr = unary();

    while (match(TokenType.MULTIPLY, TokenType.DIVIDE)) {
      Token operator = previous();
      Expression right = unary();
      expr = new BinaryExpression(expr, operator.getType(), right);
    }

    return expr;
  }

  private Expression unary() {
    if (match(TokenType.NOT, TokenType.MINUS)) {
      Token operator = previous();
      Expression right = unary();
      return new UnaryExpression(operator.getType(), right);
    }

    return primary();
  }

  private Expression primary() {
    if (match(TokenType.TRUE)) {
      return new LiteralExpression(true);
    }
    if (match(TokenType.FALSE)) {
      return new LiteralExpression(false);
    }
    if (match(TokenType.NULL)) {
      return new LiteralExpression(null);
    }

    if (match(TokenType.NUMBER)) {
      return new LiteralExpression(previous().getLiteral());
    }

    if (match(TokenType.STRING)) {
      return new LiteralExpression(previous().getLiteral());
    }

    if (match(TokenType.IDENTIFIER)) {
      return identifierOrFunctionCall();
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expression expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expected ')' after expression");
      return expr;
    }

    throw new ParseException("Expected expression", peek().getPosition());
  }

  private Expression identifierOrFunctionCall() {
    Token name = previous();
    StringBuilder variablePath = new StringBuilder(name.getLexeme());

    // Handle dot notation for nested properties
    while (match(TokenType.DOT)) {
      if (!check(TokenType.IDENTIFIER)) {
        throw new ParseException("Expected identifier after '.'", peek().getPosition());
      }
      variablePath.append(".").append(advance().getLexeme());
    }

    // Check for function call
    if (match(TokenType.LEFT_PAREN)) {
      List<Expression> arguments = new ArrayList<>();

      if (!check(TokenType.RIGHT_PAREN)) {
        do {
          arguments.add(expression());
        } while (match(TokenType.COMMA));
      }

      consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments");
      return new FunctionCallExpression(name.getLexeme(), arguments);
    }

    return new VariableExpression(variablePath.toString());
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) {
      return false;
    }
    return peek().getType() == type;
  }

  private Token advance() {
    if (!isAtEnd()) {
      current++;
    }
    return previous();
  }

  private boolean isAtEnd() {
    return peek().getType() == TokenType.EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) {
      return advance();
    }
    throw new ParseException(message, peek().getPosition());
  }
}
