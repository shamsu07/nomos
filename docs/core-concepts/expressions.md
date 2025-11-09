# Core Concepts: The Expression Engine

The `when` clause in a `nomos` rule is powered by a custom expression parser and evaluator. This engine converts your human-readable rule conditions into executable Java logic.

### Supported Operations

The expression engine supports standard operator precedence (like in Java) and a wide range of operations.

#### 1. Literals
* **Numbers:** `100`, `3.14`
* **Strings:** `"VIP Member"`, `'Premium'`
* **Booleans:** `true`, `false`
* **Null:** `null`

#### 2. Variables
* Access any key from the `Facts` object.
* `user`
* `cartTotal()`
* `user.address.city` (POJO/Map access)

#### 3. Logical Operators
* `&&` (AND)
* `||` (OR)
* `!` (NOT)

#### 4. Comparison Operators
* `==`
* `!=`
* `>`
* `>=`
* `<`
* `<=`

#### 5. Arithmetic Operators
* `+` (Addition and String concatenation)
* `-` (Subtraction and Negation)
* `*` (Multiplication)
* `/` (Division)

#### 6. Grouping
* `(` `)`: Use parentheses to control the order of evaluation.

### Examples

| Expression | Description |
| --- | --- |
| `isVIP() && cartTotal() > 100` | Combines function calls and comparisons. |
| `user.orderCount == 0` | Accesses a POJO property. |
| `(cartTotal() * 0.1) + 5.0` | Performs arithmetic. |
| `!isPremium() && user.email != null` | Uses logical NOT and a null check. |
| `user.type == 'VIP' \|\| user.type == 'PREMIUM'` | Checks if a string value is one of two options. |