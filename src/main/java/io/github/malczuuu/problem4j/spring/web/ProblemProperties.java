package io.github.malczuuu.problem4j.spring.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "problem4j")
public class ProblemProperties {

  private final boolean loggingEnabled;
  private final String defaultDetailFormat;

  public ProblemProperties(
      @DefaultValue("true") boolean loggingEnabled,
      @DefaultValue(DetailFormat.CAPITALIZED) String defaultDetailFormat) {
    this.loggingEnabled = loggingEnabled;
    this.defaultDetailFormat = defaultDetailFormat;
  }

  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }

  public String getDefaultDetailFormat() {
    return defaultDetailFormat;
  }
}
