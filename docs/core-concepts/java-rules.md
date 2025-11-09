# Core Concepts: Rule Definition (Java)

For cases where you need type-safety or complex logic that is difficult to express in a string, you can define rules directly in Java using the `Rule.builder()`.

This is an alternative to using YAML files and is useful for rules defined by developers.

### Builder Methods

* `name(String)`: The name of the rule.
* `priority(int)`: The execution priority (higher executes first).
* `when(Predicate<Facts>)`: A lambda that receives the `Facts` and returns `true` or `false`.
* `then(Rule.Action)`: A lambda that receives the `Facts` and returns the *modified* `Facts`.

### Example

Here is a rule defined entirely in Java.

```java
import io.github.shamsu07.nomos.core.rule.Rule;
import io.github.shamsu07.nomos.core.engine.RuleEngine;
import io.github.shamsu07.nomos.core.facts.Facts;
// import your User and Cart models

public class JavaRuleDefinitions {

    public void registerJavaRules(RuleEngine engine) {
    
        Rule vipRule = Rule.builder()
            .name("Java VIP Discount")
            .priority(100)
            .when(facts -> {
                User user = facts.get("user", User.class);
                Cart cart = facts.get("cart", Cart.class);
                
                return user != null 
                       && "VIP".equals(user.getType())
                       && cart != null
                       && cart.getTotal() > 100;
            })
            .then(facts -> {
                // Return a new facts instance with modifications
                return facts.put("discount.percent", 15.0)
                            .put("discount.reason", "Java VIP Rule");
            })
            .build();
            
        engine.addRule(vipRule);
    }
}