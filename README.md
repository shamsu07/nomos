# nomos

<p>
  <img alt="License" src="https://img.shields.io/badge/license-Apache_2.0-blue.svg?style=flat-square">
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
implementation("io.github.shxms.nomos:nomos-spring-boot-starter:0.0.1-SNAPSHOT")
```

Maven:

```XML

<dependency>
    <groupId>com.yourcompany.nomos</groupId>
    <artifactId>nomos-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

As a Standalone Library
Add the nomos-core dependency.

Gradle:

```Kotlin
implementation("com.yourcompany.nomos:nomos-core:0.0.1-SNAPSHOT")
```

Maven:

```XML
<dependency>
    <groupId>com.yourcompany.nomos</groupId>
    <artifactId>nomos-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
⚙️ Quick Usage (Spring Boot)
Define your rules in src/main/resources/rules/discount.yml:

```YAML

- name: "10% VIP Discount"
  priority: 100
  conditions:
    - fact: "user.type"
      operator: "EQUAL"
      value: "VIP"
    - fact: "cart.total"
      operator: "GREATER_THAN"
      value: 100
  actions:
    - type: "SET_FACT"
      target: "cart.discountPercent"
      value: 10.0
```

Configure nomos in your application.properties:

Properties

# Scan for all .yml files in the 'rules/' directory
nomos.rule-location=classpath:rules/
Inject and use the engine in your service:

```Java

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
        cart.setDiscountPercent(updatedFacts.get("cart.discountPercent"));
        return cart;
    }
}
```
🤝 Contributing
We welcome contributions! This is a new project, and we'd love your help. Please see our CONTRIBUTING.md file for details on how to get started.

📜 License
nomos is open-source software licensed under the Apache License 2.0.


---

### 2. Your "Perfectionist's Guide" to Standards

Here are the standards you should follow.

#### 1. Top-Level Project Files

Your root `nomos/` directory should look professional. Besides the `README.md`, you must add:

* **`LICENSE`:** This is non-negotiable. For a Java library aiming for enterprise use, the **Apache 2.0 License** is the standard. It's permissive and trusted. Just copy-paste the official text into this file.
* **`CONTRIBUTING.md`:** A file that explains *how* others can help.
    * How to set up the project.
    * A link to the coding style (see below).
    * The branching and PR process (e.g., "fork, branch, open PR").
* **`CODE_OF_CONDUCT.md`:** This establishes a welcoming community. Use the [Contributor Covenant](https://www.contributor-covenant.org/version/2/1/code_of_conduct.md) template.
* **`.gitignore`:** Use a standard one. [GitHub's Java .gitignore template](https://github.com/github/gitignore/blob/main/Java.gitignore) is perfect. Also add `.gradle/` and `build/`.

#### 2. Coding Style & Naming Conventions

* **Coding Style:** **Do not invent your own.** Adopt a major one. The most respected and modern one for Java is the [**Google Java Style Guide**](https://google.github.io/styleguide/javaguide.html). It covers everything from file structure to naming.
* **Naming Conventions (from the guide):**
    * **Packages:** `lowercase.with.dots` (e.g., `com.yourcompany.nomos.core`, `com.yourcompany.nomos.engine`). **I recommend using your GitHub username if you don't have a company, e.g., `io.github.shamsuddeenks.nomos.core`**.
    * **Classes/Interfaces:** `PascalCase` (e.g., `RuleEngine`, `Facts`).
    * **Methods & Variables:** `camelCase` (e.g., `execute`, `updatedFacts`).
    * **Constants:** `UPPER_SNAKE_CASE` (e.g., `DEFAULT_PRIORITY`).
* **How to Enforce It (The "Perfection" part):**
    * **`Spotless`:** Add the Spotless plugin to your root `build.gradle.kts`. It will **automatically reformat** all your code to match the Google style every time you build. It's "set it and forget it."
    * **`.editorconfig`:** Create this file in your root to enforce basic whitespace and indentation rules in every IDE.

#### 3. Git & GitHub Workflow

* **Branching:** Use a simple **GitHub Flow**.
    1.  `main` is your primary branch. It **must always be stable and deployable**.
    2.  **Never** commit directly to `main`.
    3.  Create new branches for *everything* (e.g., `feat/add-java-dsl`, `fix/yaml-parser-bug`, `docs/update-readme`).
    4.  Open a **Pull Request (PR)** to merge your branch into `main`.
* **Commit Messages:** This is key. Use [**Conventional Commits**](https://www.conventionalcommits.org/en/v1.0.0/). It's a simple prefix system that makes your history *perfectly readable* and allows for automatic changelog generation.
    * `feat: Add rule indexing for faster performance`
    * `fix: Correct null pointer in Fact parsing`
    * `docs: Update README with new Java DSL examples`
    * `style: Apply spotless formatting to core module`
    * `refactor: Simplify RuleEngine execution loop`
    * `test: Add unit tests for priority sorting`
* **GitHub Actions (CI):** This is the final piece. Create a file at `.github/workflows/build.yml`. This simple file will:
    1.  Automatically run `gradle clean build test` on every push to `main` and on every PR.
    2.  This **proves** that your project is not broken.
    3.  This is what powers the "Build: Passing" badge in your README, giving instant confidence.

This is a comprehensive, professional-grade setup. It's a lot, but you asked for perfect!

Your next step is to create these top-level files. Would you like me to provide the templates for `.gitignore`, `CONTRIBUTING.md`, and the GitHub Action workflow?