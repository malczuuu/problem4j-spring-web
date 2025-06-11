package io.github.malczuuu.problem4j.spring.web;

import io.github.malczuuu.problem4j.jackson.ProblemModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProblemProperties.class)
@Import(ProblemResponseEntityExceptionHandler.class)
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
}
