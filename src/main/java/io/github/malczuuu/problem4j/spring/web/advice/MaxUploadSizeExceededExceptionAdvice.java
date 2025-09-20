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
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class MaxUploadSizeExceededExceptionAdvice
    extends AbstractAdvice<MaxUploadSizeExceededException> {

  public MaxUploadSizeExceededExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({MaxUploadSizeExceededException.class})
  @Override
  public ResponseEntity<Problem> handle(MaxUploadSizeExceededException ex, WebRequest request) {
    HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE;
    Problem problem =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .detail("max upload size exceeded")
            .extension("maxUploadSize", ex.getMaxUploadSize())
            .build();
    return handleInternal(ex, problem, new HttpHeaders(), status, request);
  }
}
