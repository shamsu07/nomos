# Getting Started with Spring Boot

This guide will walk you through building a rule-based discount service using `nomos` and Spring Boot.

### 1. Add Dependencies

Add the `nomos-spring-boot-starter` to your project.

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

### 2. Configure Properties

In your `application.properties`, tell `nomos` where to find your rules and enable hot-reloading.

```properties
# Nomos Configuration
nomos.rule-location=classpath:rules/discount.yml
nomos.hot-reload=true
nomos.stop-on-first-applied-rule=false
```

### 3. Define Your Rules

Create `src/main/resources/rules/discount.yml`.

```yaml
rules:
  - name: "VIP Discount"
    priority: 100
    when: "isVIP() && cartTotal() > 100"
    then:
      - discount.percent = 15
      - discount.reason = "VIP Member"
      - logDiscount("VIP Member", 15)
      - sendEmail(user.email, "You got 15% off as a VIP!")

  - name: "Premium Discount"
    priority: 90
    when: "isPremium() && cartTotal() > 50"
    then:
      - discount.percent = 10
      - discount.reason = "Premium Member"
      - logDiscount("Premium Member", 10)

  - name: "Bulk Order Discount"
    priority: 80
    when: "itemCount() > 20"
    then:
      - discount.percent = 12
      - discount.reason = "Bulk Order"
      - logDiscount("Bulk Order", 12)
      - trackMetric("bulk_orders", 1)

  - name: "First Time Buyer"
    priority: 70
    when: "user.orderCount == 0 && cartTotal() > 30"
    then:
      - discount.percent = 5
      - discount.reason = "First Time Buyer"
      - logDiscount("First Time Buyer", 5)
```

### 4. Create Rule Functions

Create a class for your `when` clause functions. These are annotated with `@NomosFunction`.

```java
package io.github.shamsu07.nomos.example.rules;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import io.github.shamsu07.nomos.example.model.Cart;
import io.github.shamsu07.nomos.example.model.User;

public class DiscountFunctions {

  @NomosFunction("isVIP")
  public boolean isVIP(Facts facts) {
    User user = facts.get("user", User.class);
    return "VIP".equals(user.getType());
  }

  @NomosFunction("isPremium")
  public boolean isPremium(Facts facts) {
    User user = facts.get("user", User.class);
    return "PREMIUM".equals(user.getType());
  }

  @NomosFunction("cartTotal")
  public double cartTotal(Facts facts) {
    Cart cart = facts.get("cart", Cart.class);
    return cart.getTotal();
  }

  @NomosFunction("itemCount")
  public int itemCount(Facts facts) {
    Cart cart = facts.get("cart", Cart.class);
    return cart.getItemCount();
  }
}
```

### 5. Create Rule Actions

Create a class for your `then` clause actions. These are annotated with `@NomosAction`.

```java
package io.github.shamsu07.nomos.example.rules;

import io.github.shamsu07.nomos.core.action.NomosAction;
import io.github.shamsu07.nomos.core.facts.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscountActions {

  private static final Logger logger = LoggerFactory.getLogger(DiscountActions.class);

  @NomosAction("logDiscount")
  public void logDiscount(Facts facts, String reason, double percent) {
    logger.info("Applied {}% discount: {}", percent, reason);
  }

  @NomosAction("sendEmail")
  public void sendEmail(String email, String message) {
    logger.info("Sending email to {}: {}", email, message);
  }

  @NomosAction("trackMetric")
  public void trackMetric(String metric, Object value) {
    logger.info("Metric: {} = {}", metric, value);
  }
}
```

### 6. Register Functions & Actions

Register your new classes as Spring beans and wire them into `nomos` using the `NomosConfigurer` interface.

```java
package io.github.shamsu07.nomos.example.config;

import io.github.shamsu07.nomos.example.rules.DiscountActions;
import io.github.shamsu07.nomos.example.rules.DiscountFunctions;
import io.github.shamsu07.nomos.spring.NomosConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleConfiguration {

  @Bean
  public NomosConfigurer nomosConfigurer(DiscountFunctions functions, DiscountActions actions) {
    return (functionRegistry, actionRegistry) -> {
      functionRegistry.registerFunctionsFrom(functions);
      actionRegistry.registerActionsFrom(actions);
    };
  }

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

### 7. Execute the Engine

Inject the `ReloadableRuleEngine` into your service and use it.

```java
package io.github.shamsu07.nomos.example.service;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.reload.ReloadableRuleEngine;
import io.github.shamsu07.nomos.example.model.Cart;
import io.github.shamsu07.nomos.example.model.User;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

  private final ReloadableRuleEngine ruleEngine;

  public CheckoutService(ReloadableRuleEngine ruleEngine) {
    this.ruleEngine = ruleEngine;
  }

  public Cart applyDiscounts(User user, Cart cart) {
    // 1. Create facts
    Facts facts = new Facts();
    facts = facts.put("user", user);
    facts = facts.put("cart", cart);

    // 2. Execute rules
    Facts result = ruleEngine.execute(facts);

    // 3. Apply changes from facts
    Double discountPercent = result.get("discount.percent", Double.class);
    if (discountPercent != null) {
      cart.setDiscountPercent(discountPercent);
    }

    return cart;
  }
}
```