package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class MissingPathVariableExceptionAdvice
    extends AbstractAdvice<MissingPathVariableException> {

  public MissingPathVariableExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({MissingPathVariableException.class})
  @Override
  public ResponseEntity<Problem> handle(MissingPathVariableException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .detail("missing path variable")
            .extension("name", ex.getVariableName())
            .build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
