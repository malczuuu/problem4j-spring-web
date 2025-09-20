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

@RestControllerAdvice
public class ExceptionAdvice extends AbstractAdvice<Exception> {

  public ExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({Exception.class})
  @Override
  public ResponseEntity<Problem> handle(Exception ex, WebRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    Problem problem =
        Problem.builder().title(status.getReasonPhrase()).status(status.value()).build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
