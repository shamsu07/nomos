package io.github.shamsu07.nomos.core.expression;

import io.github.shamsu07.nomos.core.expression.ast.Expression;
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import java.util.List;
import java.util.Objects;

/**
 * Evaluates parsed expressions against facts.
 *
 * <p>Combines lexer, parser, and AST evaluation into a single interface.
 */
public final class ExpressionEvaluator {

  private final FunctionRegistry functionRegistry;

  public ExpressionEvaluator(FunctionRegistry functionRegistry) {
    this.functionRegistry =
        Objects.requireNonNull(functionRegistry, "Function registry cannot be null");
  }

  /**
   * Parse and evaluate expression string.
   *
   * @param expressionString Expression to evaluate
   * @param facts Current facts
   * @return Evaluation result
   * @throws ParseException if expression syntax is invalid
   */
  public Object evaluate(String expressionString, Facts facts) {
    Objects.requireNonNull(expressionString, "Expression string cannot be null");
    Objects.requireNonNull(facts, "Facts cannot be null");

    Expression ast = parse(expressionString);
    return ast.evaluate(facts, functionRegistry);
  }

  /**
   * Parse expression string into AST
   *
   * @param expressionString Expression to parse
   * @return Parsed expression AST
   * @throws ParseException if expression syntax is invalid
   */
  public Expression parse(String expressionString) {
    Objects.requireNonNull(expressionString, "Expression string cannot be null");

    Lexer lexer = new Lexer(expressionString);
    List<Token> tokens = lexer.tokenize();

    ExpressionParser parser = new ExpressionParser(tokens);
    return parser.parse();
  }
}
