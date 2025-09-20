package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class HttpMediaTypeNotSupportedExceptionAdvice
    extends AbstractAdvice<HttpMediaTypeNotSupportedException> {

  public HttpMediaTypeNotSupportedExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
  @Override
  public ResponseEntity<Problem> handle(HttpMediaTypeNotSupportedException ex, WebRequest request) {
    HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
    Problem problem =
        Problem.builder().title(status.getReasonPhrase()).status(status.value()).build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
