package io.github.shamsu07.nomos.core.facts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable container for rule execution context. Supports nested property access via dto notation
 * (e.g., "user.address.city"). Thread-safe dut to immutability - modifications return new
 * instances.
 */
public final class Facts {

  private final Map<String, Object> data;

  public Facts() {
    this.data = new HashMap<>();
  }

  public Facts(Map<String, Object> data) {
    this.data = deepCopy(data);
  }

  /**
   * Store a fact. Returns new Facts instance.
   *
   * @param key Fact key (supports dot annotation for nested values)
   * @param value Fact value
   * @return New Facts instance with added values
   */
  public Facts put(String key, Object value) {
    Objects.requireNonNull(key, "Fact key cannot be null");
    Map<String, Object> newData = new HashMap<>(this.data);

    if (key.contains(".")) {
      putNested(newData, key, value);
    } else {
      newData.put(key, value);
    }
    return new Facts(newData);
  }

  /**
   * Retrieve a fact with type casting
   *
   * @param key Fact key (supports dot annotation)
   * @param type Expected type
   * @return Value cast to type, or null if not foun
   */
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> type) {
    Object value = get(key);
    if (value == null) {
      return null;
    }

    if (!type.isAssignableFrom(value.getClass())) {
      throw new ClassCastException(
          String.format(
              "Fact '%s' is %s, cannot cast to %s",
              key, value.getClass().getName(), type.getName()));
    }

    return (T) value;
  }

  /**
   * Retrieve a fact as Object
   *
   * @param key Fact key (supports dot annotation like "user.email")
   * @return Value or null if not found
   */
  public Object get(String key) {
    Objects.requireNonNull(key, "Fact key cannot be null");

    if (!key.contains(".")) {
      return data.get(key);
    }

    return getNested(data, key);
  }

  /** Check if fact exists */
  public boolean contains(String key) {
    return get(key) != null;
  }

  /** Get all facts as immutable map */
  public Map<String, Object> asMap() {
    return new HashMap<>(data);
  }

  // Nested propery support - uses reflection for POJOs
  private void putNested(Map<String, Object> target, String path, Object value) {
    String[] parts = path.split("\\.", 2);
    String firstkey = parts[0];

    if (parts.length == 1) {
      target.put(firstkey, value);
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> nested =
        (Map<String, Object>) target.computeIfAbsent(firstkey, k -> new HashMap<String, Object>());

    putNested(nested, parts[1], value);
  }

  @SuppressWarnings("unchecked")
  private Object getNested(Map<String, Object> source, String path) {
    String[] parts = path.split("\\.", 2);
    Object value = source.get(parts[0]);

    if (value == null || parts.length == 1) {
      return value;
    }

    // Try as Map first
    if (value instanceof Map) {
      return getNested((Map<String, Object>) value, parts[1]);
    }

    // Fallback to reflection for POJOs
    return getFieldValue(value, parts[1]);
  }

  // Use reflection to access POJO fields
  private Object getFieldValue(Object obj, String fieldPath) {
    String[] parts = fieldPath.split("\\.", 2);
    String fieldName = parts[0];

    try {
      // Try getter method first (getField or isField)
      String getterName = "get" + capitalize(fieldName);
      Object value = obj.getClass().getMethod(getterName).invoke(obj);

      if (parts.length == 1) {
        return value;
      }

      return value != null ? getFieldValue(value, parts[1]) : null;
    } catch (NoSuchMethodException e) {
      // Try boolean getter
      try {
        String boolGetterName = "is" + capitalize(fieldName);
        Object value = obj.getClass().getMethod(boolGetterName).invoke(obj);

        if (parts.length == 1) {
          return value;
        }

        return value != null ? getFieldValue(value, parts[1]) : null;
      } catch (Exception ignored) {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  private String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }

    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> deepCopy(Map<String, Object> source) {
    Map<String, Object> copy = new HashMap<>();
    for (Map.Entry<String, Object> entry : source.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Map) {
        copy.put(entry.getKey(), deepCopy((Map<String, Object>) value));
      } else {
        copy.put(entry.getKey(), value);
      }
    }

    return copy;
  }

  @Override
  public String toString() {
    return data.toString();
  }
}
