# nomos

<p>
  <img alt="License" src="https://img.shields.io/badge/license-Apache_2.0-blue.svg?style=flat-square">
  <img alt="Build Status" src="https://img.shields.io/github/actions/workflow/status/YOUR_USERNAME/nomos/build.yml?branch=main&style=flat-square">
</p>

A modern, lightweight, and developer-first rule engine for Java and Spring Boot.

`nomos` is designed to be the simple, transparent, and fast alternative for 95% of use cases where traditional rule engines (like Drools) are heavyweight and complex.

---

### 💡 Why nomos?

Traditional rule engines are powerful but often come with:
* A steep learning curve.
* Heavy memory footprints and slow startup times.
* Complex, proprietary UIs and XML-based configuration.
* A "black box" execution model that's hard to debug.

`nomos` is different. Our philosophy is **developer-centric, simple, and transparent.**

* ✅ **Lightweight:** A small, plain Java JAR with minimal dependencies.
* ✅ **Simple DSL:** Define rules in human-readable **YAML** files or a **Fluent Java DSL**.
* ✅ **Spring-Native:** A first-class Spring Boot starter for zero-effort autoconfiguration.
* ✅ **Hot-Reloading:** Rules are treated as configuration, not code. Reload them at runtime without restarting your service.
* ✅ **Transparent:** A "glass box" design with built-in tracing and metrics (coming soon) so you always know *why* a rule fired.

### ✨ Features

* **Core Engine (`nomos-core`):** A standalone Java library. Use it in any Java project.
* **Spring Boot Starter (`nomos-spring-boot-starter`):** Seamless autoconfiguration for Spring Boot apps.
* **Multiple DSLs:**
    * **YAML:** For externalized, hot-reloadable rule sets.
    * **Java Fluent API:** For type-safe, developer-defined rules.
* **High Performance:** Simple forward-chaining logic with a focus on speed and low overhead.

---

### 🚀 Getting Started

You can use `nomos` as a standalone library or with the Spring Boot starter.

#### With Spring Boot (Recommended)

Add the `nomos-spring-boot-starter` dependency.

**Gradle:**
```kotlin
implementation("io.github.nomos:nomos-spring-boot-starter:0.0.1-SNAPSHOT")
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.nomos</groupId>
    <artifactId>nomos-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### As a Standalone Library

Add the `nomos-core` dependency.

**Gradle:**
```kotlin
implementation("io.github.nomos:nomos-core:0.0.1-SNAPSHOT")
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.nomos</groupId>
    <artifactId>nomos-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

### ⚙️ Quick Usage (Spring Boot)

Define your rules in `src/main/resources/rules/discount.yml`:

```yaml
rules:
  - name: "VIP Discount"
    priority: 100
    when: "isVIP(user) && cart.total > 100"
    then:
      - discount.percent = 10
      - discount.reason = "VIP Member"
      - sendEmail(user.email, "You got 10% off!")
```

Configure nomos in your `application.properties`:

```properties
# Scan for all .yml files in the 'rules/' directory
nomos.rule-location=classpath:rules/
```

Inject and use the engine in your service:

```java
@Service
public class CheckoutService {

    private final RuleEngine ruleEngine;

    @Autowired
    public CheckoutService(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    public Cart applyDiscounts(User user, Cart cart) {
        // 1. Create facts
        Facts facts = new Facts();
        facts.put("user", user);
        facts.put("cart", cart);

        // 2. Execute
        Facts updatedFacts = ruleEngine.execute(facts);

        // 3. Apply changes
        cart.setDiscountPercent(updatedFacts.get("discount.percent"));
        return cart;
    }
}
```

---

### 🤝 Contributing

We welcome contributions! This is a new project, and we'd love your help. Please see our [CONTRIBUTING.md](CONTRIBUTING.md) file for details on how to get started.

### 📜 License

nomos is open-source software licensed under the [Apache License 2.0](LICENSE).
