package io.github.malczuuu.problem4j.spring.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import io.github.malczuuu.problem4j.core.Problem;
import io.github.malczuuu.problem4j.spring.web.formatting.DetailFormatting;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class ProblemResponseEntityExceptionHandlerTest {

  // a mock web request is used to simulate an HTTP request context
  private final ServletWebRequest mockWebRequest =
      new ServletWebRequest(new MockHttpServletRequest());

  // utilities used for reflection-related exceptions
  private final Method method = DummyController.class.getMethod("someMethod", String.class);
  private final MethodParameter methodParameter = new MethodParameter(method, 0);

  private DetailFormatting detailFormatting;
  private ProblemResponseEntityExceptionHandler handler;

  ProblemResponseEntityExceptionHandlerTest() throws NoSuchMethodException {}

  @BeforeEach
  void beforeEach() {
    handler =
        new ProblemResponseEntityExceptionHandler(
            detail -> detail, fieldName -> fieldName, List.of());
  }

  @Test
  void givenHttpRequestMethodNotSupportedExceptionShouldGenerateProblem() {
    HttpRequestMethodNotSupportedException ex =
        new HttpRequestMethodNotSupportedException(
            HttpMethod.PUT.name(), List.of(HttpMethod.GET.name(), HttpMethod.POST.name()));

    ResponseEntity<Object> response =
        handler.handleHttpRequestMethodNotSupported(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), problem.getStatus());
    assertNull(problem.getDetail());
  }

  @Test
  void givenHttpMediaTypeNotSupportedExceptionShouldGenerateProblem() {
    HttpMediaTypeNotSupportedException ex =
        new HttpMediaTypeNotSupportedException(
            MediaType.APPLICATION_XML, List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

    ResponseEntity<Object> response =
        handler.handleHttpMediaTypeNotSupported(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), problem.getStatus());
    assertNull(problem.getDetail());
  }

  @Test
  void givenHttpMediaTypeNotAcceptableExceptionShouldGenerateProblem() {
    HttpMediaTypeNotAcceptableException ex =
        new HttpMediaTypeNotAcceptableException(
            List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN));

    ResponseEntity<Object> response =
        handler.handleHttpMediaTypeNotAcceptable(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.NOT_ACCEPTABLE.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), problem.getStatus());
    assertNull(problem.getDetail());
  }

  @Test
  void givenMissingPathVariableExceptionShouldGenerateProblem() {
    MissingPathVariableException ex =
        new MissingPathVariableException("variableName", methodParameter);

    ResponseEntity<Object> response =
        handler.handleMissingPathVariable(ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals("Missing path variable", problem.getDetail());
  }

  @Test
  void givenMissingServletRequestParameterExceptionShouldGenerateProblem() {

    MissingServletRequestParameterException ex =
        new MissingServletRequestParameterException("parameterName", "String");

    ResponseEntity<Object> response =
        handler.handleMissingServletRequestParameter(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals("Missing request param", problem.getDetail());
    assertEquals(ex.getParameterName(), problem.getExtensionValue("param"));
    assertEquals(ex.getParameterType().toLowerCase(), problem.getExtensionValue("type"));
  }

  @Test
  void givenMissingServletRequestPartExceptionShouldGenerateProblem() {
    MissingServletRequestPartException ex =
        new MissingServletRequestPartException("requestPartName");

    ResponseEntity<Object> response =
        handler.handleMissingServletRequestPart(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals("Missing request part", problem.getDetail());
    assertEquals(ex.getRequestPartName(), problem.getExtensionValue("param"));
  }

  @Test
  void givenMethodArgumentNotValidExceptionShouldGenerateProblem() {
    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(
            methodParameter, new BeanPropertyBindingResult("target", "objectName"));

    ResponseEntity<Object> response =
        handler.handleMethodArgumentNotValid(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals("Validation failed", problem.getDetail());
  }

  @Test
  void givenHandlerMethodValidationExceptionShouldGenerateProblem() {
    MethodValidationResult mockMethodValidationResult = mock(MethodValidationResult.class);
    HandlerMethodValidationException ex =
        new HandlerMethodValidationException(mockMethodValidationResult);

    ResponseEntity<Object> response =
        handler.handleHandlerMethodValidationException(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
  }

  @Test
  void givenNoHandlerFoundExceptionShouldGenerateProblem() {
    NoHandlerFoundException ex =
        new NoHandlerFoundException(HttpMethod.GET.name(), "/api/resources", new HttpHeaders());

    ResponseEntity<Object> response =
        handler.handleNoHandlerFoundException(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
  }

  @Test
  void givenNoResourceFoundExceptionShouldGenerateProblem() {
    NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/resource");

    ResponseEntity<Object> response =
        handler.handleNoResourceFoundException(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
  }

  @Test
  void givenAsyncRequestTimeoutExceptionShouldGenerateProblem() {
    AsyncRequestTimeoutException ex = new AsyncRequestTimeoutException();

    ResponseEntity<Object> response =
        handler.handleAsyncRequestTimeoutException(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
  }

  @Test
  void givenErrorResponseExceptionShouldGenerateProblem() {
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.UNAUTHORIZED);

    ResponseEntity<Object> response =
        handler.handleErrorResponseException(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.UNAUTHORIZED.value(), problem.getStatus());
  }

  @Test
  void givenMaxUploadSizeExceededExceptionShouldGenerateProblem() {

    MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024);

    ResponseEntity<Object> response =
        handler.handleMaxUploadSizeExceededException(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.value(), problem.getStatus());
    assertEquals("Max upload size exceeded", problem.getDetail());
    assertEquals(ex.getMaxUploadSize(), problem.getExtensionValue("max"));
  }

  @Test
  void givenConversionNotSupportedExceptionShouldGenerateProblem() {
    ConversionNotSupportedException ex = mock(ConversionNotSupportedException.class);

    ResponseEntity<Object> response =
        handler.handleConversionNotSupported(
            ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
    assertNull(problem.getDetail());
  }

  @Test
  void givenTypeMismatchExceptionShouldGenerateProblem() {
    TypeMismatchException ex = new TypeMismatchException("12", Integer.class);
    ex.initPropertyName("propertyName");

    ResponseEntity<Object> response =
        handler.handleTypeMismatch(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals("Type mismatch", problem.getDetail());
    assertEquals(ex.getPropertyName(), problem.getExtensionValue("property"));
    assertEquals(
        ex.getRequiredType().getSimpleName().toLowerCase(), problem.getExtensionValue("type"));
  }

  @Test
  void givenHttpMessageNotReadableExceptionShouldGenerateProblem() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException(
            "", new MockHttpInputMessage("".getBytes(StandardCharsets.UTF_8)));

    ResponseEntity<Object> response =
        handler.handleHttpMessageNotReadable(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertNull(problem.getDetail());
  }

  @Test
  void givenHttpMessageNotWritableExceptionShouldGenerateProblem() {
    HttpMessageNotWritableException ex = new HttpMessageNotWritableException("");

    ResponseEntity<Object> response =
        handler.handleHttpMessageNotWritable(
            ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
    assertNull(problem.getDetail());
  }

  @Test
  void givenMethodValidationExceptionShouldGenerateProblem() {
    MethodValidationException ex =
        new MethodValidationException(mock(MethodValidationResult.class));

    ResponseEntity<Object> response =
        handler.handleMethodValidationException(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertNull(problem.getDetail());
  }

  private static class DummyController {
    public void someMethod(String id) {}
  }
}
