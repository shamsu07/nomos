# Core Concepts: Functions vs. Actions

`nomos` makes a critical distinction between **Functions** (for checking state) and **Actions** (for changing state).

---

### Functions (`@NomosFunction`)

Functions are used in the `when` clause of a rule.

* **Purpose:** To **evaluate** state and return a value (usually `boolean`, `number`, or `string`).
* **Annotation:** `@NomosFunction("nameInYaml")`
* **Rule:** Should be **pure** and have **no side effects**. They should not change facts or call external services that change state (like a database write).
* **Registry:** Managed by `FunctionRegistry`.

#### Example

This function checks if a user is a VIP. It is called from the `when` clause.

```java
// DiscountFunctions.java
import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.NomosFunction;

public class DiscountFunctions {

  @NomosFunction("isVIP")
  public boolean isVIP(Facts facts) {
    User user = facts.get("user", User.class);
    return "VIP".equals(user.getType());
  }
}
```

```yaml
# discount.yml
rules:
  - name: "VIP Discount"
    when: "isVIP() && cartTotal() > 100" # <-- Function used here
    then:
      - ...
```

---

### Actions (`@NomosAction`)

Actions are used in the `then` clause of a rule.

* **Purpose:** To execute logic, modify facts, or perform side effects (like logging, sending emails, or saving to a database).
* **Annotation:** `@NomosAction("nameInYaml")`
* **Rule:** Can return `void` (for side effects) or `Facts` (if they modify the facts map).
* **Registry:** Managed by `ActionRegistry`.

#### Example

This action sends an email. It is called from the `then` clause.

```java
// DiscountActions.java
import io.github.shamsu07.nomos.core.action.NomosAction;
import io.github.shamsu07.nomos.core.facts.Facts;

public class DiscountActions {

  @NomosAction("sendEmail")
  public void sendEmail(String email, String message) {
    // emailService.send(email, message);
    logger.info("Sending email to {}: {}", email, message);
  }
}
```

```yaml
# discount.yml
rules:
  - name: "VIP Discount"
    when: "isVIP()"
    then:
      - sendEmail(user.email, "You got 15% off!") # <-- Action used here
```