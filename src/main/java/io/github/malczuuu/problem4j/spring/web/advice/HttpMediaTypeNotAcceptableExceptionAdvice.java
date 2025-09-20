package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class HttpMediaTypeNotAcceptableExceptionAdvice
    extends AbstractAdvice<HttpMediaTypeNotAcceptableException> {

  public HttpMediaTypeNotAcceptableExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({HttpMediaTypeNotAcceptableException.class})
  @Override
  public ResponseEntity<Problem> handle(
      HttpMediaTypeNotAcceptableException ex, WebRequest request) {
    HttpStatus status = HttpStatus.NOT_ACCEPTABLE;

    Problem problem =
        Problem.builder().title(status.getReasonPhrase()).status(status.value()).build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
