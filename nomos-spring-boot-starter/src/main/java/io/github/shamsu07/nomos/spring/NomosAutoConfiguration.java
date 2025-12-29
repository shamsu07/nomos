package io.github.shamsu07.nomos.spring;

import io.github.shamsu07.nomos.core.action.ActionRegistry;
import io.github.shamsu07.nomos.core.function.FunctionRegistry;
import io.github.shamsu07.nomos.core.reload.ReloadableRuleEngine;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

@AutoConfiguration
@EnableConfigurationProperties(NomosProperties.class)
public class NomosAutoConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(NomosAutoConfiguration.class);

  @Bean
  @ConditionalOnMissingBean
  public FunctionRegistry functionRegistry() {
    return new FunctionRegistry();
  }

  @Bean
  @ConditionalOnMissingBean
  public ActionRegistry actionRegistry() {
    return new ActionRegistry();
  }

  @Bean(destroyMethod = "close")
  @ConditionalOnMissingBean
  public ReloadableRuleEngine ruleEngine(
      FunctionRegistry functionRegistry,
      ActionRegistry actionRegistry,
      NomosProperties properties,
      List<NomosConfigurer> configurers)
      throws IOException {

    // Sort configurers by order (supports both Ordered interface and @Order annotation)
    configurers.sort(AnnotationAwareOrderComparator.INSTANCE);

    // Allow users to register custom functions/actions in order
    for (NomosConfigurer configurer : configurers) {
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Executing NomosConfigurer: {} with order: {}",
            configurer.getClass().getSimpleName(),
            configurer.getOrder());
      }
      configurer.configure(functionRegistry, actionRegistry);
    }

    ReloadableRuleEngine engine =
        new ReloadableRuleEngine(
            functionRegistry, actionRegistry, properties.isStopOnFirstAppliedRule());

    // Load rules if location specified
    if (properties.getRuleLocation() != null && !properties.getRuleLocation().isEmpty()) {
      try {
        engine.loadRules(properties.getRuleLocation(), properties.isHotReload());
        logger.info(
            "Loaded {} rules from {}", engine.getRules().size(), properties.getRuleLocation());
      } catch (IOException e) {
        if (properties.isFailOnLoadError()) {
          logger.error("Failed to load rules from {}", properties.getRuleLocation(), e);
          throw e;
        } else {
          logger.warn(
              "Failed to load rules from {} - continuing with empty rule set: {}",
              properties.getRuleLocation(),
              e.getMessage());
        }
      }
    }

    return engine;
  }
}
