package io.github.malczuuu.problem4j.spring.web.formatting;

import io.github.malczuuu.problem4j.spring.web.DetailFormat;

public class DefaultDetailFormatting implements DetailFormatting {

  private final String detailFormat;

  public DefaultDetailFormatting(String detailFormat) {
    this.detailFormat = detailFormat;
  }

  @Override
  public String format(String detail) {
    if (detailFormat == null) {
      return detail;
    }
    return switch (detailFormat.toLowerCase()) {
      case DetailFormat.LOWERCASE -> detail.toLowerCase();
      case DetailFormat.UPPERCASE -> detail.toUpperCase();
      default -> detail;
    };
  }
}
