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
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class NoHandlerFoundExceptionAdvice extends AbstractAdvice<NoHandlerFoundException> {

  public NoHandlerFoundExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({NoHandlerFoundException.class})
  @Override
  public ResponseEntity<Problem> handle(NoHandlerFoundException ex, WebRequest request) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    Problem problem =
        Problem.builder().title(status.getReasonPhrase()).status(status.value()).build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
