package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class HttpMessageNotReadableExceptionAdvice
    extends AbstractAdvice<HttpMessageNotReadableException> {

  public HttpMessageNotReadableExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({HttpMessageNotReadableException.class})
  @Override
  public ResponseEntity<Problem> handle(HttpMessageNotReadableException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder().title(status.getReasonPhrase()).status(status.value()).build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
