package io.github.malczuuu.problem4j.spring.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

public class ExceptionLoggingAdapter implements ExceptionAdapter, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ExceptionLoggingAdapter.class);

  @Override
  public void afterPropertiesSet() {
    log.info("Enabled HTTP exception logging");
  }

  @Override
  public void adapt(WebRequest request, Exception ex, Object body) {
    if (request instanceof ServletWebRequest) {
      log((ServletWebRequest) request, ex);
    } else {
      if (log.isDebugEnabled()) {
        log.debug(
            "Unhandled exception {} : {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
      } else {
        log.info("Unhandled exception {} : {}", ex.getClass().getSimpleName(), ex.getMessage());
      }
    }
  }

  private void log(ServletWebRequest request, Exception ex) {
    log(request.getRequest(), ex);
  }

  private void log(HttpServletRequest request, Exception ex) {
    if (log.isDebugEnabled()) {
      log.debug(
          "Unhandled exception {} : {} on {} {}",
          ex.getClass().getSimpleName(),
          ex.getMessage(),
          request.getMethod(),
          path(request),
          ex);
    } else {
      log.info(
          "Unhandled exception {} : {} on {} {}",
          ex.getClass().getSimpleName(),
          ex.getMessage(),
          request.getMethod(),
          path(request));
    }
  }

  private String path(HttpServletRequest req) {
    String result = req.getServletPath();
    if (req.getQueryString() != null && !req.getQueryString().isEmpty()) {
      result += "?" + req.getQueryString();
    }
    return result;
  }
}
