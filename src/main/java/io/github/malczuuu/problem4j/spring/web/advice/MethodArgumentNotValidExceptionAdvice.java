package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import io.github.malczuuu.problem4j.spring.web.FieldNameFormatting;
import io.github.malczuuu.problem4j.spring.web.Violation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class MethodArgumentNotValidExceptionAdvice
    extends AbstractAdvice<MethodArgumentNotValidException> {

  private final FieldNameFormatting fieldNameFormatting;

  public MethodArgumentNotValidExceptionAdvice(
      List<ExceptionAdapter> exceptionAdapters, FieldNameFormatting fieldNameFormatting) {
    super(exceptionAdapters);
    this.fieldNameFormatting = fieldNameFormatting;
  }

  @ExceptionHandler({MethodArgumentNotValidException.class})
  @Override
  public ResponseEntity<Problem> handle(MethodArgumentNotValidException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .detail("validation failed")
            .extension("errors", getViolations(ex.getBindingResult()))
            .build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }

  private ArrayList<Violation> getViolations(BindingResult bindingResult) {
    ArrayList<Violation> violations = new ArrayList<>();
    bindingResult
        .getFieldErrors()
        .forEach(
            f ->
                violations.add(
                    new Violation(
                        fieldNameFormatting.format(f.getField()), f.getDefaultMessage())));
    bindingResult
        .getGlobalErrors()
        .forEach(g -> violations.add(new Violation(null, g.getDefaultMessage())));
    return violations;
  }
}
