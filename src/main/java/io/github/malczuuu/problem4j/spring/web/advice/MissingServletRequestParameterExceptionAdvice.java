package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class MissingServletRequestParameterExceptionAdvice
    extends AbstractAdvice<MissingServletRequestParameterException> {

  public MissingServletRequestParameterExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({MissingServletRequestParameterException.class})
  @Override
  public ResponseEntity<Problem> handle(
      MissingServletRequestParameterException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    Problem problem =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .detail("missing request parameter")
            .extension("param", ex.getParameterName())
            .extension("type", ex.getParameterType().toLowerCase())
            .build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
