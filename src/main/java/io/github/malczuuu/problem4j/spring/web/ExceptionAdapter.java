package io.github.malczuuu.problem4j.spring.web;

import org.springframework.web.context.request.WebRequest;

public interface ExceptionAdapter {

  void adapt(WebRequest request, Exception ex, Object body);
}
