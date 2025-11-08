package io.github.shamsu07.nomos.example.controller;

import io.github.shamsu07.nomos.example.model.Cart;
import io.github.shamsu07.nomos.example.model.User;
import io.github.shamsu07.nomos.example.service.CheckoutService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

  private final CheckoutService checkoutService;

  public CheckoutController(CheckoutService checkoutService) {
    this.checkoutService = checkoutService;
  }

  @GetMapping("/calculate")
  public CheckoutResponse calculateDiscount(
      @RequestParam String userType, @RequestParam double total, @RequestParam int itemCount) {

    User user = new User("user-123", "Test User", "test@example.com", userType);
    Cart cart = new Cart("cart-123", total, itemCount);

    Cart result = checkoutService.applyDiscounts(user, cart);

    return new CheckoutResponse(
        result.getTotal(), result.getDiscountPercent(), result.getFinalTotal());
  }

  record CheckoutResponse(double originalTotal, double discountPercent, double finalTotal) {}
}
