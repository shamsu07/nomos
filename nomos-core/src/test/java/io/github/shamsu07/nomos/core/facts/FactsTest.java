package io.github.shamsu07.nomos.core.facts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class FactsTest {
  @Test
  void should_storeAndRetrieve_when_simpleFact() {
    Facts facts = new Facts().put("name", "John");
    assertEquals("John", facts.get("name"));
  }

  @Test
  void should_returnTypedValue_when_getWithClass() {
    Facts facts = new Facts().put("age", 25);
    Integer age = facts.get("age", Integer.class);
    assertEquals(25, age);
  }

  @Test
  void should_throwClassCastException_when_wrongType() {
    Facts facts = new Facts().put("age", 25);
    assertThrows(ClassCastException.class, () -> facts.get("age", String.class));
  }

  @Test
  void should_returnNull_when_factNotFound() {
    Facts facts = new Facts();
    assertNull(facts.get("missing"));
  }

  @Test
  void should_returnTrue_when_factExists() {
    Facts facts = new Facts().put("key", "value");
    assertTrue(facts.contains("key"));
  }

  @Test
  void should_returnFalse_when_factDoesNotExist() {
    Facts facts = new Facts();
    assertFalse(facts.contains("missing"));
  }

  @Test
  void should_storeNestedFact_when_dotNotation() {
    Facts facts = new Facts().put("user.name", "Alice");
    assertEquals("Alice", facts.get("user.name"));
  }

  @Test
  void should_retrieveNestedfact_when_multiLevel() {
    Facts facts = new Facts().put("user.address.city", "NYC");
    assertEquals("NYC", facts.get("user.address.city"));
  }

  @Test
  void should_accessPOJOProperty_when_objectStored() {
    User user = new User("Bob", 30);
    Facts facts = new Facts().put("user", user);
    assertEquals("Bob", facts.get("user.name"));
    assertEquals(30, facts.get("user.age"));
  }

  @Test
  void should_returnNewInstance_when_putCalled() {
    Facts original = new Facts().put("a", 1);
    Facts modified = original.put("b", 2);
    assertNotSame(original, modified);
    assertNull(original.get("b"));
    assertEquals(2, modified.get("b"));
  }

  @Test
  void should_throwNullPointerException_when_nullKey() {
    Facts facts = new Facts();
    assertThrows(NullPointerException.class, () -> facts.put(null, "value"));
    assertThrows(NullPointerException.class, () -> facts.get(null));
  }

  @Test
  void should_returnMap_when_asMapCalled() {
    Facts facts = new Facts().put("a", 1).put("b", 2);
    Map<String, Object> map = facts.asMap();
    assertEquals(2, map.size());
    assertEquals(1, map.get("a"));
    assertEquals(2, map.get("b"));
  }

  @Test
  void should_returnBooleanProperty_when_isGetter() {
    User user = new User("Test", 20);
    user.setActive(true);
    Facts facts = new Facts().put("user", user);
    assertEquals(true, facts.get("user.active"));
  }

  static class User {
    private String name;
    private int age;
    private Address address;
    private boolean active;

    public User(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public User(String name, int age, Address address) {
      this.name = name;
      this.age = age;
      this.address = address;
    }

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }

    public Address getAddress() {
      return address;
    }

    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
    }
  }

  static class Address {
    private String city;
    private String state;

    public Address(String city, String state) {
      this.city = city;
      this.state = state;
    }

    public String getCity() {
      return city;
    }

    public String getState() {
      return state;
    }
  }
}
