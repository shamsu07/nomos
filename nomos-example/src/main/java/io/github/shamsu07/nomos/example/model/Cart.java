package io.github.shamsu07.nomos.example.model;

public class Cart {
  private String id;
  private double total;
  private int itemCount;
  private double discountPercent;

  public Cart(String id, double total, int itemCount) {
    this.id = id;
    this.total = total;
    this.itemCount = itemCount;
    this.discountPercent = 0.0;
  }

  public double getFinalTotal() {
    return total * (1 - discountPercent / 100);
  }

  // Getters and setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public double getTotal() {
    return total;
  }

  public void setTotal(double total) {
    this.total = total;
  }

  public int getItemCount() {
    return itemCount;
  }

  public void setItemCount(int itemCount) {
    this.itemCount = itemCount;
  }

  public double getDiscountPercent() {
    return discountPercent;
  }

  public void setDiscountPercent(double discountPercent) {
    this.discountPercent = discountPercent;
  }
}
