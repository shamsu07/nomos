package io.github.shamsu07.nomos.example.service;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.reload.ReloadableRuleEngine;
import io.github.shamsu07.nomos.example.model.Cart;
import io.github.shamsu07.nomos.example.model.User;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

  private final ReloadableRuleEngine ruleEngine;

  public CheckoutService(ReloadableRuleEngine ruleEngine) {
    this.ruleEngine = ruleEngine;
  }

  public Cart applyDiscounts(User user, Cart cart) {
    // Create facts
    Facts facts = new Facts();
    facts = facts.put("user", user);
    facts = facts.put("cart", cart);

    // Execute rules
    Facts result = ruleEngine.execute(facts);

    // Apply discount if calculated
    Double discountPercent = result.get("discount.percent", Double.class);
    if (discountPercent != null) {
      cart.setDiscountPercent(discountPercent);
    }

    return cart;
  }
}
