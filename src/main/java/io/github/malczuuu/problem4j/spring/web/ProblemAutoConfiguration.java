package io.github.malczuuu.problem4j.spring.web;

import io.github.malczuuu.problem4j.jackson.ProblemModule;
import io.github.malczuuu.problem4j.spring.web.formatting.DefaultDetailFormatting;
import io.github.malczuuu.problem4j.spring.web.formatting.DetailFormatting;
import io.github.malczuuu.problem4j.spring.web.formatting.FieldNameFormatting;
import io.github.malczuuu.problem4j.spring.web.formatting.JacksonFieldNameFormatting;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProblemProperties.class)
public class ProblemAutoConfiguration {

  @ConditionalOnMissingBean(ProblemModule.class)
  @Bean
  public ProblemModule problemModule() {
    return new ProblemModule();
  }

  @ConditionalOnProperty(
      name = "problem4j.logging-enabled",
      havingValue = "true",
      matchIfMissing = true)
  @Bean
  public ExceptionLoggingAdapter exceptionLoggingAdapter() {
    return new ExceptionLoggingAdapter();
  }

  @ConditionalOnMissingBean(DetailFormatting.class)
  @Bean
  public DetailFormatting detailFormatting(ProblemProperties properties) {
    return new DefaultDetailFormatting(properties.getDefaultDetailFormat());
  }

  @ConditionalOnMissingBean(FieldNameFormatting.class)
  @Bean
  public FieldNameFormatting fieldNameFormatting(JacksonProperties properties) {
    return new JacksonFieldNameFormatting(properties.getPropertyNamingStrategy());
  }

  @ConditionalOnMissingBean(ProblemResponseEntityExceptionHandler.class)
  @Bean
  public ProblemResponseEntityExceptionHandler problemResponseEntityExceptionHandler(
      DetailFormatting detailFormatting,
      FieldNameFormatting fieldNameFormatting,
      List<ExceptionAdapter> exceptionAdapters) {
    return new ProblemResponseEntityExceptionHandler(
        detailFormatting, fieldNameFormatting, exceptionAdapters);
  }
}
