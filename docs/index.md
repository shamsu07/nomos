---
hide:
  - navigation
  - toc
---

<div class="hero">
  <div class="hero__badge">
    <span class="hero__badge-dot"></span>
    <span>v1.0 Now Available</span>
  </div>

  <img class="hero__logo" src="assets/nomos-logo.png" alt="nomos logo" style="height: 175px;">

  <h1 class="hero__title">Rules made simple.</h1>

  <p class="hero__subtitle">
    A lightweight, blazing-fast rule engine for Java. Define business rules in
    YAML or Java, hot-reload without restarts, and always know why a rule fired.
  </p>

  <div class="hero__actions">
    <a href="getting-started/" class="md-button md-button--primary">
      Get Started
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
    </a>
    <a href="https://github.com/shamsu07/nomos" class="md-button md-button--secondary">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>
      View on GitHub
    </a>
  </div>
</div>

---

## Why nomos?

Traditional rule engines are powerful but often come with a steep learning curve, heavy memory footprints, and a "black box" execution model. **nomos** is different.

<div class="feature-grid">
  <div class="feature-card">
    <div class="feature-card__icon">⚡</div>
    <h3 class="feature-card__title">Blazing Fast</h3>
    <p class="feature-card__description">
      100K+ rule evaluations per second. Pre-compiled expressions and zero reflection in the hot path.
    </p>
  </div>

  <div class="feature-card">
    <div class="feature-card__icon">🪶</div>
    <h3 class="feature-card__title">Lightweight</h3>
    <p class="feature-card__description">
      Under 100KB JAR size with minimal dependencies. No bloat, just what you need.
    </p>
  </div>

  <div class="feature-card">
    <div class="feature-card__icon">🔄</div>
    <h3 class="feature-card__title">Hot Reload</h3>
    <p class="feature-card__description">
      Update rules at runtime without restarts. Changes take effect in under 50ms.
    </p>
  </div>

  <div class="feature-card">
    <div class="feature-card__icon">🔍</div>
    <h3 class="feature-card__title">Transparent</h3>
    <p class="feature-card__description">
      Built-in execution tracing. Always know exactly which rules fired and why.
    </p>
  </div>

  <div class="feature-card">
    <div class="feature-card__icon">🌱</div>
    <h3 class="feature-card__title">Spring Native</h3>
    <p class="feature-card__description">
      First-class Spring Boot starter with auto-configuration. Zero boilerplate setup.
    </p>
  </div>

  <div class="feature-card">
    <div class="feature-card__icon">📝</div>
    <h3 class="feature-card__title">Developer First</h3>
    <p class="feature-card__description">
      Define rules in readable YAML or fluent Java DSL. Your choice, your way.
    </p>
  </div>
</div>

---

## Quick Start

Get up and running in minutes:

=== "Maven"

    ```xml
    <dependency>
      <groupId>io.github.shamsu07</groupId>
      <artifactId>nomos-spring-boot-starter</artifactId>
      <version>1.0.0</version>
    </dependency>
    ```

=== "Gradle"

    ```kotlin
    implementation("io.github.shamsu07:nomos-spring-boot-starter:1.0.0")
    ```

Define your first rule in YAML:

```yaml title="rules/discount.yml"
rules:
  - name: "VIP Discount"
    priority: 100
    when: "customer.tier == 'VIP' && cart.total > 100"
    then:
      - discount.percent = 15
      - discount.reason = "VIP member discount"
```

Execute rules in your service:

```java title="CheckoutService.java"
@Service
public class CheckoutService {

    private final ReloadableRuleEngine engine;

    public Order checkout(Customer customer, Cart cart) {
        Facts facts = new Facts()
            .put("customer", customer)
            .put("cart", cart);

        Facts result = engine.execute(facts);

        Double discount = result.get("discount.percent", Double.class);
        // Apply discount...
    }
}
```

That's it! Your rules are now live and will hot-reload when you edit the YAML file.

---

## Built for Production

<div class="feature-grid">
  <div class="feature-card">
    <div class="feature-card__icon">🛡️</div>
    <h3 class="feature-card__title">Thread Safe</h3>
    <p class="feature-card__description">
      Fully concurrent with atomic rule swaps during reload. No locks on execution.
    </p>
  </div>

  <div class="feature-card">
    <div class="feature-card__icon">✅</div>
    <h3 class="feature-card__title">Fail Fast</h3>
    <p class="feature-card__description">
      Validate rules at load time, not runtime. Catch errors before they hit production.
    </p>
  </div>

  <div class="feature-card">
    <div class="feature-card__icon">📊</div>
    <h3 class="feature-card__title">Observable</h3>
    <p class="feature-card__description">
      Execution traces, reload events, and metrics for complete visibility.
    </p>
  </div>
</div>

---

<div style="text-align: center; padding: 3rem 0;">
  <h2 style="margin-bottom: 1rem;">Ready to simplify your rules?</h2>
  <p style="color: var(--nomos-text-secondary); margin-bottom: 2rem;">
    Join developers who've replaced complex rule engines with nomos.
  </p>
  <a href="getting-started/" class="md-button md-button--primary">
    Read the Docs
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
  </a>
</div>
