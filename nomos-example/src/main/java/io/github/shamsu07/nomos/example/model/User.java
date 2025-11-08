package io.github.shamsu07.nomos.example.model;

public class User {
  private String id;
  private String name;
  private String email;
  private String type;
  private int orderCount;

  public User(String id, String name, String email, String type) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.type = type;
    this.orderCount = 0;
  }

  // Getters and setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getOrderCount() {
    return orderCount;
  }

  public void setOrderCount(int orderCount) {
    this.orderCount = orderCount;
  }
}
