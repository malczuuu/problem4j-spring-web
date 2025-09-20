package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.core.ProblemException;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ProblemExceptionAdvice extends AbstractAdvice<ProblemException> {

  public ProblemExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({ProblemException.class})
  @Override
  public ResponseEntity<Problem> handle(ProblemException ex, WebRequest request) {
    return handleInternal(
        ex,
        ex.getProblem(),
        new HttpHeaders(),
        HttpStatus.valueOf(ex.getProblem().getStatus()),
        request);
  }
}
