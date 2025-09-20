package io.github.malczuuu.problem4j.spring.web;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class JacksonFieldNameFormatting implements FieldNameFormatting {

  private final String propertyNamingStrategy;

  public JacksonFieldNameFormatting(String propertyNamingStrategy) {
    this.propertyNamingStrategy = propertyNamingStrategy;
  }

  @Override
  public String format(String fieldName) {
    if (propertyNamingStrategy == null) {
      return fieldName;
    }
    return switch (propertyNamingStrategy) {
      case "SNAKE_CASE" ->
          ((PropertyNamingStrategies.SnakeCaseStrategy) PropertyNamingStrategies.SNAKE_CASE)
              .translate(fieldName);
      case "UPPER_CAMEL_CASE" ->
          ((PropertyNamingStrategies.UpperCamelCaseStrategy)
                  PropertyNamingStrategies.UPPER_CAMEL_CASE)
              .translate(fieldName);
      case "KEBAB_CASE" ->
          ((PropertyNamingStrategies.KebabCaseStrategy) PropertyNamingStrategies.KEBAB_CASE)
              .translate(fieldName);
      case "LOWER_CASE" ->
          ((PropertyNamingStrategies.LowerCaseStrategy) PropertyNamingStrategies.LOWER_CASE)
              .translate(fieldName);
      default -> fieldName;
    };
  }
}
