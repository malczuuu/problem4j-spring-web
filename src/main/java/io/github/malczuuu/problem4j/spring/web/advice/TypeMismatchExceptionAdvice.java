package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.core.ProblemBuilder;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class TypeMismatchExceptionAdvice extends AbstractAdvice<TypeMismatchException> {

  public TypeMismatchExceptionAdvice(List<ExceptionAdapter> exceptionAdapters) {
    super(exceptionAdapters);
  }

  @ExceptionHandler({TypeMismatchException.class})
  @Override
  public ResponseEntity<Problem> handle(TypeMismatchException ex, WebRequest request) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    ProblemBuilder builder =
        Problem.builder()
            .title(status.getReasonPhrase())
            .status(status.value())
            .detail("type mismatch");

    if (ex.getPropertyName() != null) {
      builder = builder.extension("param", ex.getPropertyName());
    }

    if (ex.getRequiredType() != null) {
      builder = builder.extension("type", ex.getRequiredType().getSimpleName().toLowerCase());
    }

    return handleInternal(ex, builder.build(), new HttpHeaders(), status, request);
  }
}
