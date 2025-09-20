package io.github.malczuuu.problem4j.spring.web.advice;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.ExceptionAdapter;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public abstract class AbstractAdvice<T extends Exception> {

  private final List<ExceptionAdapter> exceptionAdapters;

  public AbstractAdvice(List<ExceptionAdapter> exceptionAdapters) {
    this.exceptionAdapters = exceptionAdapters;
  }

  public abstract ResponseEntity<Problem> handle(T ex, WebRequest request);

  protected ResponseEntity<Problem> handleInternal(
      Exception ex,
      Problem problem,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    if (headers.getContentType() == null) {
      headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    exceptionAdapters.forEach(e -> e.adapt(request, ex, problem));
    return new ResponseEntity<>(problem, headers, status);
  }
}
