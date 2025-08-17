package io.github.malczuuu.problem4j.spring.web;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.KebabCaseStrategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCaseStrategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.UpperCamelCaseStrategy;
import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.core.ProblemBuilder;
import io.github.malczuuu.problem4j.core.ProblemException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ProblemResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  private final JacksonProperties jacksonProperties;
  private final ProblemProperties problemProperties;

  private final List<ExceptionAdapter> adapters;

  public ProblemResponseEntityExceptionHandler(
      JacksonProperties jacksonProperties,
      ProblemProperties problemProperties,
      List<ExceptionAdapter> adapters) {
    this.jacksonProperties = jacksonProperties;
    this.problemProperties = problemProperties;
    this.adapters = adapters;
  }

  @ExceptionHandler({ProblemException.class})
  public ResponseEntity<Object> handleProblemException(ProblemException ex, WebRequest request) {
    Problem problem = ex.getProblem();
    HttpHeaders headers = new HttpHeaders();
    HttpStatusCode status = HttpStatus.valueOf(problem.getStatus());
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    List<Violation> errors =
        ex.getConstraintViolations().stream()
            .map(violation -> new Violation(getPropertyName(violation), violation.getMessage()))
            .toList();

    Problem problem =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .extension("errors", errors)
            .build();
    return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleOtherException(Exception ex, WebRequest request) {
    HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;
    Problem problem =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(getValidationFailedDetail())
            .build();
    return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
  }

  private String getReasonPhrase(HttpStatusCode statusCode) {
    HttpStatus status = HttpStatus.resolve(statusCode.value());
    if (status != null) {
      return status.getReasonPhrase();
    }
    return "";
  }

  private String getPropertyName(ConstraintViolation<?> violation) {
    if (violation.getPropertyPath() == null) {
      return "";
    }

    String lastElement = null;
    for (Path.Node node : violation.getPropertyPath()) {
      lastElement = node.getName();
    }

    return lastElement != null ? lastElement : "";
  }

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.METHOD_NOT_ALLOWED;
    ProblemBuilder builder =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(getMethodNotSupportedDetail(ex));
    if (ex.getSupportedMethods() != null) {
      builder.extension("supportedMethods", Arrays.asList(ex.getSupportedMethods()));
    }
    Problem problem = builder.build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  private String getMethodNotSupportedDetail(HttpRequestMethodNotSupportedException ex) {
    String detailTemplate = "Method {} not supported";
    detailTemplate = postProcessDetailTemplate(detailTemplate);
    return detailTemplate.formatted(ex.getMethod());
  }

  private String postProcessDetailTemplate(String detailTemplate) {
    return switch (problemProperties.getDefaultDetailFormat()) {
      case DetailFormat.LOWERCASE -> detailTemplate.toLowerCase();
      case DetailFormat.UPPERCASE -> detailTemplate.toUpperCase();
      default -> detailTemplate;
    };
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
    Problem problem =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(getMediaTypeNotSupportedDetail(ex))
            .extension("supportedMediaTypes", new ArrayList<>(ex.getSupportedMediaTypes()))
            .build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  private String getMediaTypeNotSupportedDetail(HttpMediaTypeNotSupportedException ex) {
    String detailTemplate = "Media type {} not supported";
    detailTemplate = postProcessDetailTemplate(detailTemplate);
    return detailTemplate.formatted(ex.getContentType());
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.NOT_ACCEPTABLE;
    Problem problem =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .extension("supportedMediaTypes", new ArrayList<>(ex.getSupportedMediaTypes()))
            .build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMissingPathVariable(
      MissingPathVariableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(getMissingPathVariableDetail(ex))
            .extension("name", ex.getVariableName())
            .build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  private String getMissingPathVariableDetail(MissingPathVariableException ex) {
    String detailTemplate = "Missing {} path variable";
    detailTemplate = postProcessDetailTemplate(detailTemplate);
    return detailTemplate.formatted(ex.getVariableName());
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(getMissingServletRequestParameterDetail(ex))
            .extension("param", ex.getParameterName())
            .extension("type", ex.getParameterType().toLowerCase())
            .build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  private String getMissingServletRequestParameterDetail(
      MissingServletRequestParameterException ex) {
    String detailTemplate = "Missing {} request param of type {}";
    detailTemplate = postProcessDetailTemplate(detailTemplate);
    return detailTemplate.formatted(ex.getParameterName(), ex.getParameterType().toLowerCase());
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
      MissingServletRequestPartException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(getMissingServletRequestPartDetail(ex))
            .extension("param", ex.getRequestPartName())
            .build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  private String getMissingServletRequestPartDetail(MissingServletRequestPartException ex) {
    String detailTemplate = "Missing {} request part";
    detailTemplate = postProcessDetailTemplate(detailTemplate);
    return detailTemplate.formatted(ex.getRequestPartName());
  }

  @Override
  protected ResponseEntity<Object> handleServletRequestBindingException(
      ServletRequestBindingException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(ex.getMessage())
            .build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    Problem problem =
        from(ex.getBindingResult()).title(getReasonPhrase(status)).status(status.value()).build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  private ProblemBuilder from(BindingResult bindingResult) {
    ArrayList<Violation> details = new ArrayList<>();
    bindingResult
        .getFieldErrors()
        .forEach(f -> details.add(new Violation(fieldName(f.getField()), f.getDefaultMessage())));
    bindingResult
        .getGlobalErrors()
        .forEach(g -> details.add(new Violation(null, g.getDefaultMessage())));
    return Problem.builder().detail(getValidationFailedDetail()).extension("errors", details);
  }

  private String fieldName(String field) {
    if (jacksonProperties.getPropertyNamingStrategy() == null) {
      return field;
    }
    return switch (jacksonProperties.getPropertyNamingStrategy()) {
      case "SNAKE_CASE" ->
          ((SnakeCaseStrategy) PropertyNamingStrategies.SNAKE_CASE).translate(field);
      case "UPPER_CAMEL_CASE" ->
          ((UpperCamelCaseStrategy) PropertyNamingStrategies.UPPER_CAMEL_CASE).translate(field);
      case "KEBAB_CASE" ->
          ((KebabCaseStrategy) PropertyNamingStrategies.KEBAB_CASE).translate(field);
      case "LOWER_CASE" ->
          ((LowerCaseStrategy) PropertyNamingStrategies.LOWER_CASE).translate(field);
      default -> field;
    };
  }

  private String getValidationFailedDetail() {
    String detailTemplate = "Validation failed";
    detailTemplate = postProcessDetailTemplate(detailTemplate);
    return detailTemplate;
  }

  @Override
  protected ResponseEntity<Object> handleHandlerMethodValidationException(
      HandlerMethodValidationException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    Problem body = Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    // TODO: debug how to extract validation violations
    return handleExceptionInternal(ex, body, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
      NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    status = HttpStatus.NOT_FOUND;
    Problem problem =
        Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleNoResourceFoundException(
      NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    status = HttpStatus.NOT_FOUND;
    Problem problem =
        Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
      AsyncRequestTimeoutException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.INTERNAL_SERVER_ERROR;
    Problem problem =
        Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleErrorResponseException(
      ErrorResponseException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    ProblemBuilder builder =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(ex.getBody().getDetail())
            .instance(ex.getBody().getInstance());

    if (ex.getBody().getProperties() != null) {
      for (Map.Entry<String, Object> entry : ex.getBody().getProperties().entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        builder = builder.extension(key, value);
      }
    }

    Problem problem = builder.build();

    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.PAYLOAD_TOO_LARGE;
    Problem problem =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(getMaxUploadSizeExceededDetail())
            .extension("maxUploadSize", ex.getMaxUploadSize())
            .build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  private String getMaxUploadSizeExceededDetail() {
    String detailTemplate = "Max upload size exceeded";
    detailTemplate = postProcessDetailTemplate(detailTemplate);
    return detailTemplate;
  }

  @Override
  protected ResponseEntity<Object> handleConversionNotSupported(
      ConversionNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.INTERNAL_SERVER_ERROR;
    Problem problem =
        Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    ProblemBuilder problemBuilder =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(getTypeMismatchDetail(ex));
    if (ex.getRequiredType() != null) {
      problemBuilder =
          problemBuilder.extension("type", ex.getRequiredType().getSimpleName().toLowerCase());
    }
    return handleExceptionInternal(ex, problemBuilder.build(), headers, status, request);
  }

  private String getTypeMismatchDetail(TypeMismatchException ex) {
    String detailTemplate = "Type mismatch of {} property";
    detailTemplate = postProcessDetailTemplate(detailTemplate);
    return detailTemplate.formatted(ex.getPropertyName());
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotWritable(
      HttpMessageNotWritableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.INTERNAL_SERVER_ERROR;
    Problem problem =
        Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodValidationException(
      MethodValidationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    Problem body = Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    // TODO: debug how to extract validation violations
    return handleExceptionInternal(ex, body, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    headers = new HttpHeaders(headers);
    Object finalBody =
        body != null
            ? body
            : Problem.builder().title(getReasonPhrase(status)).status(status.value()).build();
    if (body instanceof Problem) {
      headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }
    adapters.forEach(e -> e.adapt(request, ex, finalBody));
    return super.handleExceptionInternal(ex, body, headers, status, request);
  }
}
