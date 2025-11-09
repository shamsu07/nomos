# Core Concepts: Rule Definition (YAML)

The YAML file is the simplest way to define and manage your rules. The `YAMLRuleLoader` parses this file into executable `Rule` objects.

### File Structure

A rule file must contain a top-level `rules` key, which is a list of rule definitions.

```yaml
rules:
  - name: "Rule 1"
    ...
  - name: "Rule 2"
    ...
```

---

### Rule Properties

Each rule in the list has four properties:

* `name` (String, Required): A unique name for the rule. Used for logging and tracing.
* `priority` (Integer, Optional): Controls execution order. Higher numbers execute first. Defaults to `0`.
* `when` (String, Required): An expression that must evaluate to `true` for the rule to fire.
* `then` (List, Required): A list of actions to execute if the rule fires.

---

### The `then` Action Syntax

The `then` block supports two types of actions:

#### 1. Assignment

This modifies the `Facts` object directly. It uses a simple `key = value` syntax. The value on the right is evaluated as an expression.

```yaml
then:
  # Set a simple literal value
  - discount.percent = 15
  
  # Set a string value
  - discount.reason = "VIP Member"
  
  # Set based on an expression
  - user.loyaltyPoints = user.loyaltyPoints + 10
```

#### 2. Action Function Call

This calls a registered `@NomosAction`. It uses standard function call syntax.

```yaml
then:
  # Call an action with no arguments
  - logEvent()
  
  # Call with literal arguments
  - logDiscount("VIP Member", 15)
  
  # Call with variables from facts
  - sendEmail(user.email, "You got 15% off!")
```

---

### Full Example

This rule combines all concepts:

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
```