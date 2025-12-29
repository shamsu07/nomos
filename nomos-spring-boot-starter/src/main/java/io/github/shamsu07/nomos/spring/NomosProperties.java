package io.github.shamsu07.nomos.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nomos")
public class NomosProperties {

  /** Location of rule files (supports classpath: and file:) */
  private String ruleLocation = "classpath:rules/";

  /** Enable hot reloading of rules */
  private boolean hotReload = false;

  /** Stop after first matching rule fires */
  private boolean stopOnFirstAppliedRule = false;

  /** Fail application startup if rules cannot be loaded (default: true for fail-fast behavior) */
  private boolean failOnLoadError = true;

  public String getRuleLocation() {
    return ruleLocation;
  }

  public void setRuleLocation(String ruleLocation) {
    this.ruleLocation = ruleLocation;
  }

  public boolean isHotReload() {
    return hotReload;
  }

  public void setHotReload(boolean hotReload) {
    this.hotReload = hotReload;
  }

  public boolean isStopOnFirstAppliedRule() {
    return stopOnFirstAppliedRule;
  }

  public void setStopOnFirstAppliedRule(boolean stopOnFirstAppliedRule) {
    this.stopOnFirstAppliedRule = stopOnFirstAppliedRule;
  }

  public boolean isFailOnLoadError() {
    return failOnLoadError;
  }

  public void setFailOnLoadError(boolean failOnLoadError) {
    this.failOnLoadError = failOnLoadError;
  }
}
