# Core Concepts: The Facts Object

The `Facts` object is the central data container for rule execution. It holds all the input data your rules need to make decisions.

### Key Features

* **Immutable-by-Default:** The `put()` method returns a *new* `Facts` instance with the added data. This prevents rules from interfering with each other's state and makes execution predictable.
* **POJO Access:** You can put plain Java objects (POJOs) into the facts.
* **Dot-Notation:** `nomos` supports dot-notation (e.g., `user.address.city`) to access nested properties on maps or POJOs using their getter methods (`getAddress().getCity()`).
* **Type-Safe Getters:** You can retrieve facts with type safety using `facts.get("key", String.class)`.

### Usage Example

```java
// 1. Create initial facts
Facts facts = new Facts();

// 2. Add data
// Note: Each .put() returns a new instance
facts = facts.put("user", new User("user-123", "Test User", "test@example.com", "VIP"));
facts = facts.put("cart", new Cart("cart-123", 150.0, 25));
facts = facts.put("discount.percent", 0.0); // Can set nested properties

// 3. Retrieve data
String email = facts.get("user.email", String.class); // "test@example.com"
Double total = facts.get("cart.total", Double.class); // 150.0
Double percent = facts.get("discount.percent", Double.class); // 0.0
```