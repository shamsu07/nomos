package io.github.shamsu07.nomos.example.rules;

import io.github.shamsu07.nomos.core.facts.Facts;
import io.github.shamsu07.nomos.core.function.NomosFunction;
import io.github.shamsu07.nomos.example.model.Cart;
import io.github.shamsu07.nomos.example.model.User;

public class DiscountFunctions {

  @NomosFunction("isVIP")
  public boolean isVIP(Facts facts) {
    User user = facts.get("user", User.class);
    return "VIP".equals(user.getType());
  }

  @NomosFunction("isPremium")
  public boolean isPremium(Facts facts) {
    User user = facts.get("user", User.class);
    return "PREMIUM".equals(user.getType());
  }

  @NomosFunction("cartTotal")
  public double cartTotal(Facts facts) {
    Cart cart = facts.get("cart", Cart.class);
    return cart.getTotal();
  }

  @NomosFunction("itemCount")
  public int itemCount(Facts facts) {
    Cart cart = facts.get("cart", Cart.class);
    return cart.getItemCount();
  }
}
