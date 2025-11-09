# Contributing to Nomos

Thank you for your interest in contributing to Nomos! We welcome contributions from the community.

## Getting Started

1. **Fork the repository** and clone it locally
2. **Create a branch** for your feature or fix: `git checkout -b feat/your-feature-name`
3. **Make your changes** following our coding standards
4. **Test your changes** - ensure all tests pass
5. **Submit a Pull Request** with a clear description

## Development Setup

### Prerequisites
- Java 17 or higher
- Gradle 8.x (wrapper included)

### Build and Test
```bash
./gradlew clean build test
```

## Coding Standards

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

### Key Rules
- **Packages**: `lowercase` (e.g., `io.github.username.nomos.engine`)
- **Classes**: `PascalCase` (e.g., `RuleEngine`)
- **Methods/Variables**: `camelCase` (e.g., `registerFunction`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_PRIORITY`)
- **Line length**: Maximum 120 characters
- **Indentation**: 4 spaces (no tabs)

### Code Formatting
We use Spotless to enforce consistent formatting. Before committing:
```bash
./gradlew spotlessApply
```

This will auto-format your code to match our standards.

## Comments
- Explain **WHY**, not WHAT
- Focus on non-obvious decisions and trade-offs
- Avoid redundant comments

## Testing
- Write tests for all public APIs
- Test names: `should_<expected>_when_<condition>`
- Ensure >80% code coverage

## Commit Messages

We use [Conventional Commits](https://www.conventionalcommits.org/):

- `feat: Add hot reload support`
- `fix: Correct null pointer in expression parser`
- `docs: Update README with examples`
- `style: Apply spotless formatting`
- `refactor: Simplify rule execution logic`
- `test: Add unit tests for function registry`

## Pull Request Process

1. **Update documentation** if you change APIs
2. **Add tests** for new functionality
3. **Ensure CI passes** - all builds and tests must succeed
4. **Request review** from maintainers
5. **Address feedback** promptly

## Performance Considerations

Nomos prioritizes performance. When contributing:
- Avoid reflection in hot paths
- Use MethodHandles for dynamic invocation
- Pre-size collections when possible
- Profile changes that affect core engine

## Questions?

Open an issue with the `question` label, and we'll help you get started.

## Code of Conduct

Please read our [Code of Conduct](CODE_OF_CONDUCT.md) before contributing.

---

We appreciate your contributions to making Nomos better!
