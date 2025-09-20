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
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

@RestControllerAdvice
public class AsyncRequestTimeoutExceptionAdvice
    extends AbstractAdvice<AsyncRequestTimeoutException> {

  public AsyncRequestTimeoutExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler(AsyncRequestTimeoutException.class)
  @Override
  public ResponseEntity<Problem> handle(AsyncRequestTimeoutException ex, WebRequest request) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    Problem problem =
        Problem.builder().title(status.getReasonPhrase()).status(status.value()).build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
