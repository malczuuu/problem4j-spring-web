package io.github.malczuuu.problem4j.spring.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "problem4j")
public class ProblemProperties {

  private final boolean loggingEnabled;

  public ProblemProperties(@DefaultValue("true") boolean loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
  }

  public boolean isLoggingEnabled() {
    return loggingEnabled;
  }
}
