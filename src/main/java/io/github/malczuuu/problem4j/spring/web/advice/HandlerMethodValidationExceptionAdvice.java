package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class HandlerMethodValidationExceptionAdvice
    extends AbstractAdvice<HandlerMethodValidationException> {

  public HandlerMethodValidationExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({HandlerMethodValidationException.class})
  @Override
  public ResponseEntity<Problem> handle(HandlerMethodValidationException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder().title(status.getReasonPhrase()).status(status.value()).build();
    // TODO: debug how to extract validation violations
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
