package io.github.malczuuu.problem4j.spring.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "problem4j")
public class ProblemProperties {

  private final String defaultDetailFormat;

  public ProblemProperties(@DefaultValue(DetailFormat.CAPITALIZED) String defaultDetailFormat) {
    this.defaultDetailFormat = defaultDetailFormat;
  }

  public String getDefaultDetailFormat() {
    return defaultDetailFormat;
  }
}
