# Spring Boot Starter

The `nomos-spring-boot-starter` provides deep integration with Spring Boot, handling auto-configuration, property binding, and bean registration.

### 1. Auto-Configuration

By simply including the starter, `nomos` will:
1.  Create a singleton `ReloadableRuleEngine` bean and register it in the application context.
2.  Create a `FunctionRegistry` and `ActionRegistry` bean.
3.  Read `application.properties` (or `.yml`) for `nomos.` prefixed properties.
4.  Automatically load rules from `nomos.rule-location`.
5.  Set up the `FileWatcher` if `nomos.hot-reload` is true.

### 2. Configuration Properties

You can configure the engine in your `application.properties`.

| Property | Default | Description |
| --- | --- | --- |
| `nomos.rule-location` | `classpath:rules/` | Location of rule files. Supports `classpath:` and `file:` prefixes. |
| `nomos.hot-reload` | `false` | If true, watches the `rule-location` for changes and reloads automatically. |
| `nomos.stop-on-first-applied-rule` | `false` | If true, stops execution after the first rule with a matching condition fires. |

### 3. Registering Functions & Actions

The easiest way to register your custom functions and actions is to:
1.  Define your `DiscountFunctions` and `DiscountActions` classes (as shown in the Getting Started guide).
2.  Register them as standard Spring `@Bean`s.
3.  Create a `@Bean` that implements the `NomosConfigurer` interface.

`nomos` will automatically find this configurer bean and use it to wire your functions and actions into the engine before loading the rules.

```java
// src/main/java/io/github/shamsu07/nomos/example/config/RuleConfiguration.java
package io.github.shamsu07.nomos.example.config;

import io.github.shamsu07.nomos.example.rules.DiscountActions;
import io.github.shamsu07.nomos.example.rules.DiscountFunctions;
import io.github.shamsu07.nomos.spring.NomosConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleConfiguration {

  // This bean is picked up by NomosAutoConfiguration
  @Bean
  public NomosConfigurer nomosConfigurer(DiscountFunctions functions, DiscountActions actions) {
    return (functionRegistry, actionRegistry) -> {
      functionRegistry.registerFunctionsFrom(functions);
      actionRegistry.registerActionsFrom(actions);
    };
  }

  // Define your functions and actions as beans
  @Bean
  public DiscountFunctions discountFunctions() {
    return new DiscountFunctions();
  }

  @Bean
  public DiscountActions discountActions() {
    return new DiscountActions();
  }
}
```