package io.github.malczuuu.problem4j.spring.web;

import io.github.malczuuu.problem4j.jackson.ProblemModule;
import io.github.malczuuu.problem4j.spring.web.advice.AsyncRequestTimeoutExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.ConstraintViolationExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.ConversionNotSupportedExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.ErrorResponseExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.ExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.HandlerMethodValidationExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.HttpMediaTypeNotAcceptableExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.HttpMediaTypeNotSupportedExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.HttpMessageNotReadableExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.HttpMessageNotWritableExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.HttpRequestMethodNotSupportedExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.MaxUploadSizeExceededExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.MethodArgumentNotValidExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.MethodValidationExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.MissingPathVariableExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.MissingServletRequestParameterExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.MissingServletRequestPartExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.NoHandlerFoundExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.NoResourceFoundExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.ProblemExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.ServletRequestBindingExceptionAdvice;
import io.github.malczuuu.problem4j.spring.web.advice.TypeMismatchExceptionAdvice;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProblemProperties.class)
public class ProblemAutoConfiguration {

  public static final int ADVICE_ORDER_1 = 500;
  public static final int ADVICE_ORDER_2 = 600;
  public static final int ADVICE_ORDER_3 = 700;
  public static final int ADVICE_ORDER_4 = 800;
  public static final int ADVICE_ORDER_5 = 900;

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

  @ConditionalOnMissingBean(FieldNameFormatting.class)
  @Bean
  public FieldNameFormatting fieldNameFormatting(JacksonProperties properties) {
    return new JacksonFieldNameFormatting(properties.getPropertyNamingStrategy());
  }

  @ConditionalOnMissingBean(AsyncRequestTimeoutExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public AsyncRequestTimeoutExceptionAdvice asyncRequestTimeoutExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new AsyncRequestTimeoutExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(ConstraintViolationExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public ConstraintViolationExceptionAdvice constraintViolationExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters, FieldNameFormatting fieldNameFormatting) {
    return new ConstraintViolationExceptionAdvice(exceptionAdapters, fieldNameFormatting);
  }

  @ConditionalOnMissingBean(ConversionNotSupportedExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public ConversionNotSupportedExceptionAdvice conversionNotSupportedExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new ConversionNotSupportedExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(ErrorResponseExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public ErrorResponseExceptionAdvice errorResponseExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new ErrorResponseExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(ExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_5)
  public ExceptionAdvice exceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    return new ExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(HandlerMethodValidationExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public HandlerMethodValidationExceptionAdvice handlerMethodValidationExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new HandlerMethodValidationExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(HttpMediaTypeNotAcceptableExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public HttpMediaTypeNotAcceptableExceptionAdvice httpMediaTypeNotAcceptableExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new HttpMediaTypeNotAcceptableExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(HttpMediaTypeNotSupportedExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public HttpMediaTypeNotSupportedExceptionAdvice httpMediaTypeNotSupportedExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new HttpMediaTypeNotSupportedExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(HttpMessageNotReadableExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public HttpMessageNotReadableExceptionAdvice httpMessageNotReadableExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new HttpMessageNotReadableExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(HttpMessageNotWritableExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public HttpMessageNotWritableExceptionAdvice httpMessageNotWritableExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new HttpMessageNotWritableExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(HttpRequestMethodNotSupportedExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public HttpRequestMethodNotSupportedExceptionAdvice httpRequestMethodNotSupportedExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new HttpRequestMethodNotSupportedExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(MaxUploadSizeExceededExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public MaxUploadSizeExceededExceptionAdvice maxUploadSizeExceededExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new MaxUploadSizeExceededExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(MethodArgumentNotValidExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public MethodArgumentNotValidExceptionAdvice methodArgumentNotValidExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters, FieldNameFormatting fieldNameFormatting) {
    return new MethodArgumentNotValidExceptionAdvice(exceptionAdapters, fieldNameFormatting);
  }

  @ConditionalOnMissingBean(MethodValidationExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public MethodValidationExceptionAdvice methodValidationExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters, FieldNameFormatting fieldNameFormatting) {
    return new MethodValidationExceptionAdvice(exceptionAdapters, fieldNameFormatting);
  }

  @ConditionalOnMissingBean(MissingPathVariableExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_3)
  public MissingPathVariableExceptionAdvice missingPathVariableExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new MissingPathVariableExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(MissingServletRequestParameterExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_3)
  public MissingServletRequestParameterExceptionAdvice
      missingServletRequestParameterExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    return new MissingServletRequestParameterExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(MissingServletRequestPartExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public MissingServletRequestPartExceptionAdvice missingServletRequestPartExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new MissingServletRequestPartExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(NoHandlerFoundExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public NoHandlerFoundExceptionAdvice noHandlerFoundExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new NoHandlerFoundExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(NoResourceFoundExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public NoResourceFoundExceptionAdvice noResourceFoundExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new NoResourceFoundExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(ProblemExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public ProblemExceptionAdvice problemExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    return new ProblemExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(ServletRequestBindingExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public ServletRequestBindingExceptionAdvice servletRequestBindingExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new ServletRequestBindingExceptionAdvice(exceptionAdapters);
  }

  @ConditionalOnMissingBean(TypeMismatchExceptionAdvice.class)
  @Bean
  @Order(ADVICE_ORDER_4)
  public TypeMismatchExceptionAdvice typeMismatchExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters) {
    return new TypeMismatchExceptionAdvice(exceptionAdapters);
  }
}
