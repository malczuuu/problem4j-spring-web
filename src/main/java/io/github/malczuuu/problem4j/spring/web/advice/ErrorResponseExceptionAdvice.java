package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.core.ProblemBuilder;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ErrorResponseExceptionAdvice extends AbstractAdvice<ErrorResponseException> {

  public ErrorResponseExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({ErrorResponseException.class})
  @Override
  public ResponseEntity<Problem> handle(ErrorResponseException ex, WebRequest request) {
    HttpStatus status = HttpStatus.valueOf(ex.getBody().getStatus());

    ProblemBuilder builder =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .detail(ex.getBody().getDetail())
            .instance(ex.getBody().getInstance());

    if (ex.getBody().getProperties() != null) {
      for (Map.Entry<String, Object> entry : ex.getBody().getProperties().entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        builder = builder.extension(key, value);
      }
    }

    return handleInternal(ex, builder.build(), new HttpHeaders(), status, request);
  }
}
