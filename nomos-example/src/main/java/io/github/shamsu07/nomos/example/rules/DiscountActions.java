package io.github.shamsu07.nomos.example.rules;

import io.github.shamsu07.nomos.core.action.NomosAction;
import io.github.shamsu07.nomos.core.facts.Facts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscountActions {

  private static final Logger logger = LoggerFactory.getLogger(DiscountActions.class);

  @NomosAction("logDiscount")
  public void logDiscount(Facts facts, String reason, double percent) {
    logger.info("Applied {}% discount: {}", percent, reason);
  }

  @NomosAction("sendEmail")
  public void sendEmail(String email, String message) {
    logger.info("Sending email to {}: {}", email, message);
  }

  @NomosAction("trackMetric")
  public void trackMetric(String metric, Object value) {
    logger.info("Metric: {} = {}", metric, value);
  }
}
