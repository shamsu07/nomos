package io.github.shamsu07.nomos.example.config;

import io.github.shamsu07.nomos.example.rules.DiscountActions;
import io.github.shamsu07.nomos.example.rules.DiscountFunctions;
import io.github.shamsu07.nomos.spring.NomosConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleConfiguration {

  @Bean
  public NomosConfigurer nomosConfigurer(DiscountFunctions functions, DiscountActions actions) {
    return (functionRegistry, actionRegistry) -> {
      functionRegistry.registerFunctionsFrom(functions);
      actionRegistry.registerActionsFrom(actions);
    };
  }

  @Bean
  public DiscountFunctions discountFunctions() {
    return new DiscountFunctions();
  }

  @Bean
  public DiscountActions discountActions() {
    return new DiscountActions();
  }
}
