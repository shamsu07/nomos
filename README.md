
# Nomos

<p>
  <img alt="License" src="https://img.shields.io/badge/license-Apache_2.0-blue.svg?style=flat-square">
  <img alt="Build Status" src="https://img.shields.io/github/actions/workflow/status/shamsu07/nomos/build.yaml?branch=main&style=flat-square">
  <img alt="Java" src="https://img.shields.io/badge/Java-17+-blue.svg?style=flat-square">
</p>

**A lightweight, high-performance rule engine for Java and Spring Boot.**

Nomos is designed for developers who need a fast, simple rule engine without the complexity of traditional solutions like Drools. Perfect for startups and mid-size companies where developers own rule configuration.

---

## 🎯 Philosophy

- **Lightweight**: <100KB core JAR, minimal dependencies  
- **Fast**: >100K rule evaluations/second, <50ms hot reload  
- **Developer-First**: Java/YAML DSL, not GUI. Code is configuration.  
- **Transparent**: Glass-box execution with tracing  
- **Modern**: Java 17+, functional style, annotation-based registration  

---

## ⚡ Performance

Nomos is optimized for speed through:

- **MethodHandles**: ~6.5x faster than reflection  
- **Facts Caching** for nested property access  
- **Zero-allocation** expression evaluation  
- **JMH Benchmarks** verifying all optimizations  

**Target performance goals:**

- `>100,000` rule evaluations/sec  
- `<50ms` hot reload  
- `<100ms` cold start  

---

## ✨ Features

### Core Engine
- Forward-chaining execution with priorities  
- Custom expression parser  
- Nested access: `user.address.city`  
- Logical/Comparison/Arithmetic operators  
- Function calls in conditions  
- Action execution with side effects  
- Full execution tracing  

### Developer Experience
- YAML DSL for rules  
- Type-safe Java Fluent API  
- Annotation-based registration  
- Hot reload via file watching  
- Fail-fast validation  
- Spring Boot Starter  

### Advanced
- Immutable `Facts`  
- Thread-safe registries  
- Execution result with fired rules  
- Stop-on-first-match mode  

---

## 🚀 Quick Start

### 1. Add Dependency

**Gradle (Kotlin DSL):**
```kotlin
implementation("io.github.shamsu07.nomos:nomos-spring-boot-starter:0.0.1-SNAPSHOT")
````

**Maven:**

```xml
<dependency>
    <groupId>io.github.shamsu07.nomos</groupId>
    <artifactId>nomos-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

For standalone usage:

```kotlin
implementation("io.github.shamsu07.nomos:nomos-core:0.0.1-SNAPSHOT")
```

---

### 2. Define Rules

Create `src/main/resources/rules/discount.yml`:

```yaml
rules:
  - name: "VIP Discount"
    priority: 100
    when: "isVIP() && cartTotal() > 100"
    then:
      discount.percent: 15
      discount.reason: "VIP Member"
      sendEmail: ["{{ user.email }}", "You got 15% off!"]

  - name: "Bulk Order Discount"
    priority: 90
    when: "itemCount() > 20"
    then:
      discount.percent: 12
      discount.reason: "Bulk Order"
```

---

### 3. Register Functions and Actions

```java
@Component
public class DiscountFunctions {

    @NomosFunction("isVIP")
    public boolean isVIP(Facts facts) {
        User user = facts.get("user", User.class);
        return "VIP".equals(user.getType());
    }

    @NomosFunction("cartTotal")
    public double cartTotal(Facts facts) {
        Cart cart = facts.get("cart", Cart.class);
        return cart.getTotal();
    }
}
```

```java
@Component
public class DiscountActions {

    @NomosAction("sendEmail")
    public void sendEmail(String email, String message) {
        emailService.send(email, message);
    }
}
```

---

### 4. Configure Spring Boot

```properties
nomos.rule-location=classpath:rules/discount.yml
nomos.hot-reload=true
```

---

### 5. Execute Rules

```java
@Service
public class CheckoutService {

    private final ReloadableRuleEngine ruleEngine;

    public Cart applyDiscounts(User user, Cart cart) {
        Facts facts = new Facts()
            .put("user", user)
            .put("cart", cart);

        Facts result = ruleEngine.execute(facts);

        Double discount = result.get("discount.percent", Double.class);
        if (discount != null) {
            cart.setDiscountPercent(discount);
        }
        return cart;
    }
}
```

---

## 📖 Documentation

### Expression Syntax

```yaml
# Literals
when: "true"
when: "42"
when: "3.14"
when: "\"hello\""

# Variables
when: "age > 18"
when: "user.address.city == \"NYC\""

# Arithmetic
when: "balance + bonus > 1000"

# Comparisons
when: "status == \"ACTIVE\""

# Logical
when: "isVIP() && balance > 100"

# Function calls
when: "calculateScore() > 80"
```

---

### Java Fluent API

```java
Rule rule = Rule.builder()
    .name("VIP Discount")
    .priority(100)
    .when(facts -> {
        User user = facts.get("user", User.class);
        Cart cart = facts.get("cart", Cart.class);
        return "VIP".equals(user.getType()) && cart.getTotal() > 100;
    })
    .then(facts -> {
        facts.put("discount.percent", 15.0);
        facts.put("discount.reason", "VIP Member");
    })
    .build();

ruleEngine.addRule(rule);
```

---

### Execution Tracing

```java
RuleEngine.ExecutionResult result = ruleEngine.executeWithTrace(facts);

System.out.println("Fired Rules: " + result.firedRuleCount());

for (RuleEngine.RuleExecution exec : result.executionTrace()) {
    System.out.printf("%s - %s (priority: %d)%n",
        exec.ruleName(),
        exec.fired() ? "FIRED" : "SKIPPED",
        exec.priority());
}
```

---

### Hot Reload

```java
ReloadableRuleEngine engine =
        new ReloadableRuleEngine(functionRegistry, actionRegistry);

engine.loadRules("file:///path/to/rules.yml", true);

// Manual reload
engine.reload();
```

---

## 🏗️ Architecture

```
┌───────────────────────────────┐
│       ReloadableRuleEngine    │
│    (FileWatcher + HotReload)  │
└──────────────┬────────────────┘
               │
┌──────────────▼───────────────┐
│          RuleEngine          │
│  Priority + Forward Chaining │
│       Execution Tracing      │
└───────────┬───────────┬──────┘
            │           │
┌───────────▼─────┐   ┌───▼────────────┐
│ FunctionRegistry│   │ ActionRegistry │
│ (MethodHandles) │   │ (MethodHandles)│
└─────────────────┘   └────────────────┘

            │
            ▼
┌──────────────────────────────┐
│     ExpressionEvaluator      │
│ Custom Parser + Cached AST   │
└──────────────────────────────┘
```

---

## 📊 Benchmarks

Run:

```bash
./gradlew jmh
```

**Sample Results (i7-9750H):**

```
Benchmark                                     Mode   Score     Units
simpleRuleExecution                           thrpt  145234    ops/s
complexRuleExecution                          thrpt  12456     ops/s
reloadRules                                   avgt   38.234    ms
```

---

## 🗂️ Project Structure

```
nomos/
├── nomos-core/
│   ├── engine/
│   ├── rule/
│   ├── facts/
│   ├── function/
│   ├── action/
│   ├── expression/
│   ├── loader/
│   └── reload/
│
├── nomos-spring-boot-starter/
│   ├── NomosAutoConfiguration
│   ├── NomosProperties
│   └── NomosConfigurer
│
└── nomos-example/
    ├── DiscountFunctions
    ├── DiscountActions
    └── CheckoutService
```

---

## 🤔 Why Not Drools?

| Feature        | Nomos           | Drools     |
| -------------- | --------------- | ---------- |
| JAR Size       | <100KB          | ~50MB      |
| Cold Start     | <100ms          | 1–2s       |
| Learning Curve | Minutes         | Days/Weeks |
| DSL            | YAML + Java     | DRL        |
| Configuration  | Developer-owned | BA-driven  |
| Dependencies   | 2               | 50+        |
| Complexity     | Simple          | Complex    |

Nomos is ideal when you want speed, simplicity, and control.

---

## 🛠️ Development

Prerequisites:

* Java 17+
* Gradle 8.x

Build:

```bash
./gradlew clean build test
```

Format:

```bash
./gradlew spotlessApply
```

Run Example:

```bash
cd nomos-example
./gradlew bootRun
```

Test Endpoint:

```bash
curl "http://localhost:8080/api/checkout/calculate?userType=VIP&total=150&itemCount=5"
```

---

## 🤝 Contributing

See **CONTRIBUTING.md**.

1. Fork & clone
2. Create branch
3. Implement & test
4. Run spotless
5. Open PR

---

## 📝 License

Nomos is licensed under the **Apache License 2.0**.

---

## 🔗 Links

* GitHub: [https://github.com/shamsu07/nomos](https://github.com/shamsu07/nomos)
* Issues: [https://github.com/shamsu07/nomos/issues](https://github.com/shamsu07/nomos/issues)
* Example App: `/nomos-example`

---

**Built with ❤️ for developers who want fast, simple rules without the complexity.**

