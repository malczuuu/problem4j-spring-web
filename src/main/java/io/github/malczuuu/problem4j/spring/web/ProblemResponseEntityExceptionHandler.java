package io.github.malczuuu.problem4j.spring.web;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.core.ProblemBuilder;
import io.github.malczuuu.problem4j.core.ProblemException;
import io.github.malczuuu.problem4j.spring.web.formatting.DetailFormatting;
import io.github.malczuuu.problem4j.spring.web.formatting.FieldNameFormatting;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
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

  private final DetailFormatting detailFormatting;
  private final FieldNameFormatting fieldNameFormatting;
  private final List<ExceptionAdapter> exceptionAdapters;

  public ProblemResponseEntityExceptionHandler(
      DetailFormatting detailFormatting,
      FieldNameFormatting fieldNameFormatting,
      List<ExceptionAdapter> exceptionAdapters) {
    this.detailFormatting = detailFormatting;
    this.fieldNameFormatting = fieldNameFormatting;
    this.exceptionAdapters = exceptionAdapters;
  }

  @ExceptionHandler({ProblemException.class})
  public ResponseEntity<Object> handleProblemException(ProblemException ex, WebRequest request) {
    Problem problem = ex.getProblem();
    HttpHeaders headers = new HttpHeaders();
    HttpStatus status = HttpStatus.valueOf(problem.getStatus());
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolationException(
      ConstraintViolationException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    List<Violation> errors =
        ex.getConstraintViolations().stream()
            .map(
                violation ->
                    new Violation(
                        fieldNameFormatting.format(fetchViolationProperty(violation)),
                        violation.getMessage()))
            .toList();

    ProblemBuilder builder =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .title(detailFormatting.format("Validation failed"))
            .extension("errors", errors);
    return handleExceptionInternal(ex, builder.build(), new HttpHeaders(), status, request);
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleOtherException(Exception ex, WebRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    ProblemBuilder builder =
        Problem.builder().title(status.getReasonPhrase()).status(status.value());
    return handleExceptionInternal(ex, builder.build(), new HttpHeaders(), status, request);
  }

  private String getReasonPhrase(HttpStatusCode statusCode) {
    if (statusCode instanceof HttpStatus status) {
      return status.getReasonPhrase();
    }

    HttpStatus status = HttpStatus.resolve(statusCode.value());
    if (status != null) {
      return status.getReasonPhrase();
    }

    return "";
  }

  private String fetchViolationProperty(ConstraintViolation<?> violation) {
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
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.NOT_ACCEPTABLE;
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMissingPathVariable(
      MissingPathVariableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    ProblemBuilder builder =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(detailFormatting.format("Missing path variable"))
            .extension("name", ex.getVariableName());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    ProblemBuilder builder =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(detailFormatting.format("Missing request param"))
            .extension("param", ex.getParameterName())
            .extension("type", ex.getParameterType().toLowerCase());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
      MissingServletRequestPartException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    ProblemBuilder builder =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(detailFormatting.format("Missing request part"))
            .extension("param", ex.getRequestPartName());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleServletRequestBindingException(
      ServletRequestBindingException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    ProblemBuilder builder =
        from(ex.getBindingResult()).title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  private ProblemBuilder from(BindingResult bindingResult) {
    ArrayList<Violation> details = new ArrayList<>();
    bindingResult
        .getFieldErrors()
        .forEach(
            f ->
                details.add(
                    new Violation(
                        fieldNameFormatting.format(f.getField()), f.getDefaultMessage())));
    bindingResult
        .getGlobalErrors()
        .forEach(g -> details.add(new Violation(null, g.getDefaultMessage())));
    return Problem.builder()
        .detail(detailFormatting.format("Validation failed"))
        .extension("errors", details);
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
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleNoResourceFoundException(
      NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    status = HttpStatus.NOT_FOUND;
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
      AsyncRequestTimeoutException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.INTERNAL_SERVER_ERROR;
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
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

    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.PAYLOAD_TOO_LARGE;
    ProblemBuilder builder =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(detailFormatting.format("Max upload size exceeded"))
            .extension("max", ex.getMaxUploadSize());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleConversionNotSupported(
      ConversionNotSupportedException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.INTERNAL_SERVER_ERROR;
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    ProblemBuilder builder =
        Problem.builder()
            .title(getReasonPhrase(status))
            .status(status.value())
            .detail(detailFormatting.format("Type mismatch"));

    if (ex.getPropertyName() != null) {
      builder = builder.extension("property", ex.getPropertyName());
    }
    if (ex.getRequiredType() != null) {
      builder = builder.extension("type", ex.getRequiredType().getSimpleName().toLowerCase());
    }
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.BAD_REQUEST;
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotWritable(
      HttpMessageNotWritableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    status = HttpStatus.INTERNAL_SERVER_ERROR;
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodValidationException(
      MethodValidationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    ProblemBuilder builder =
        Problem.builder().title(getReasonPhrase(status)).status(status.value());
    return handleExceptionInternal(ex, builder.build(), headers, status, request);
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
    exceptionAdapters.forEach(e -> e.adapt(request, ex, finalBody));
    return super.handleExceptionInternal(ex, body, headers, status, request);
  }
}
