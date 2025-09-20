package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import io.github.malczuuu.problem4j.spring.web.FieldNameFormatting;
import io.github.malczuuu.problem4j.spring.web.Violation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ConstraintViolationExceptionAdvice
    extends AbstractAdvice<ConstraintViolationException> {

  private final FieldNameFormatting fieldNameFormatting;

  public ConstraintViolationExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters, FieldNameFormatting fieldNameFormatting) {
    super(exceptionAdapters);
    this.fieldNameFormatting = fieldNameFormatting;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @Override
  public ResponseEntity<Problem> handle(ConstraintViolationException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    List<Violation> errors =
        ex.getConstraintViolations().stream()
            .map(
                violation ->
                    new Violation(
                        fieldNameFormatting.format(fetchViolationProperty(violation)),
                        violation.getMessage()))
            .toList();

    Problem problem =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .detail("validation failed")
            .extension("errors", errors)
            .build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
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
}
