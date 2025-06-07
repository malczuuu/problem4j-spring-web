package io.github.malczuuu.problem4j.spring.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.malczuuu.problem4j.core.Problem;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
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

class ProblemResponseEntityExceptionHandlerTests {

  private final ServletWebRequest mockWebRequest =
      new ServletWebRequest(new MockHttpServletRequest());
  private final MethodParameter mockMethodParameter =
      new MethodParameter(DummyController.class.getMethod("someMethod", String.class), 0);

  private ProblemProperties problemProperties;
  private ProblemResponseEntityExceptionHandler handler;

  ProblemResponseEntityExceptionHandlerTests() throws NoSuchMethodException {}

  @BeforeEach
  void beforeEach() {
    problemProperties = mock(ProblemProperties.class);
    handler =
        new ProblemResponseEntityExceptionHandler(
            new JacksonProperties(), problemProperties, List.of());
  }

  @ParameterizedTest
  @CsvSource({
    DetailFormat.LOWERCASE + ",method {} not supported",
    DetailFormat.UPPERCASE + ",METHOD {} NOT SUPPORTED",
    DetailFormat.CAPITALIZED + ",Method {} not supported"
  })
  void givenHttpRequestMethodNotSupportedExceptionShouldGenerateProblem(
      String defaultDetailFormat, String detailTemplate) {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(defaultDetailFormat);

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
    assertEquals(detailTemplate.formatted(HttpMethod.PUT), problem.getDetail());
    assertEquals(
        Arrays.asList(ex.getSupportedMethods()), problem.getExtensionValue("supportedMethods"));
  }

  @ParameterizedTest
  @CsvSource({
    DetailFormat.LOWERCASE + ",media type {} not supported",
    DetailFormat.UPPERCASE + ",MEDIA TYPE {} NOT SUPPORTED",
    DetailFormat.CAPITALIZED + ",Media type {} not supported"
  })
  void givenHttpMediaTypeNotSupportedExceptionShouldGenerateProblem(
      String defaultDetailFormat, String detailTemplate) {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(defaultDetailFormat);

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
    assertEquals(detailTemplate.formatted(HttpMethod.PUT), problem.getDetail());
    assertEquals(ex.getSupportedMediaTypes(), problem.getExtensionValue("supportedMediaTypes"));
  }

  @Test
  void givenHttpMediaTypeNotAcceptableExceptionShouldGenerateProblem() {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(DetailFormat.CAPITALIZED);

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
    assertEquals(ex.getSupportedMediaTypes(), problem.getExtensionValue("supportedMediaTypes"));
  }

  @ParameterizedTest
  @CsvSource({
    DetailFormat.LOWERCASE + ",missing {} path variable",
    DetailFormat.UPPERCASE + ",MISSING {} PATH VARIABLE",
    DetailFormat.CAPITALIZED + ",Missing {} path variable"
  })
  void givenMissingPathVariableExceptionShouldGenerateProblem(
      String defaultDetailFormat, String detailTemplate) {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(defaultDetailFormat);

    MissingPathVariableException ex =
        new MissingPathVariableException("variableName", mockMethodParameter);

    ResponseEntity<Object> response =
        handler.handleMissingPathVariable(ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals(detailTemplate.formatted(ex.getVariableName()), problem.getDetail());
    assertEquals(ex.getVariableName(), problem.getExtensionValue("name"));
  }

  @ParameterizedTest
  @CsvSource({
    DetailFormat.LOWERCASE + ",missing {} request param of type {}",
    DetailFormat.UPPERCASE + ",MISSING {} REQUEST PARAM OF TYPE {}",
    DetailFormat.CAPITALIZED + ",Missing {} request param of type {}"
  })
  void givenMissingServletRequestParameterExceptionShouldGenerateProblem(
      String defaultDetailFormat, String detailTemplate) {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(defaultDetailFormat);

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
    assertEquals(detailTemplate.formatted(ex.getParameterName()), problem.getDetail());
    assertEquals(ex.getParameterName(), problem.getExtensionValue("param"));
    assertEquals(ex.getParameterType().toLowerCase(), problem.getExtensionValue("type"));
  }

  @ParameterizedTest
  @CsvSource({
    DetailFormat.LOWERCASE + ",missing {} request part",
    DetailFormat.UPPERCASE + ",MISSING {} REQUEST PART",
    DetailFormat.CAPITALIZED + ",Missing {} request part"
  })
  void givenMissingServletRequestPartExceptionShouldGenerateProblem(
      String defaultDetailFormat, String detailTemplate) {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(defaultDetailFormat);

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
    assertEquals(detailTemplate.formatted(ex.getRequestPartName()), problem.getDetail());
    assertEquals(ex.getRequestPartName(), problem.getExtensionValue("param"));
  }

  @ParameterizedTest
  @CsvSource({
    DetailFormat.LOWERCASE + ",validation failed",
    DetailFormat.UPPERCASE + ",VALIDATION FAILED",
    DetailFormat.CAPITALIZED + ",Validation failed"
  })
  void givenMethodArgumentNotValidExceptionShouldGenerateProblem(
      String defaultDetailFormat, String detailTemplate) {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(defaultDetailFormat);

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(
            mockMethodParameter, new BeanPropertyBindingResult("target", "objectName"));

    ResponseEntity<Object> response =
        handler.handleMethodArgumentNotValid(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals(detailTemplate, problem.getDetail());
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

  // Max upload size exceeded
  @ParameterizedTest
  @CsvSource({
    DetailFormat.LOWERCASE + ",max upload size exceeded",
    DetailFormat.UPPERCASE + ",MAX UPLOAD SIZE EXCEEDED",
    DetailFormat.CAPITALIZED + ",Max upload size exceeded"
  })
  void givenMaxUploadSizeExceededExceptionShouldGenerateProblem(
      String defaultDetailFormat, String detailTemplate) {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(defaultDetailFormat);

    MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024);

    ResponseEntity<Object> response =
        handler.handleMaxUploadSizeExceededException(
            ex, ex.getHeaders(), ex.getStatusCode(), mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.PAYLOAD_TOO_LARGE.value(), problem.getStatus());
    assertEquals(detailTemplate, problem.getDetail());
    assertEquals(ex.getMaxUploadSize(), problem.getExtensionValue("maxUploadSize"));
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
  }

  @ParameterizedTest
  @CsvSource({
    DetailFormat.LOWERCASE + ",type mismatch of {} property",
    DetailFormat.UPPERCASE + ",TYPE MISMATCH OF {} PROPERTY",
    DetailFormat.CAPITALIZED + ",Type mismatch of {} property"
  })
  void givenTypeMismatchExceptionShouldGenerateProblem(
      String defaultDetailFormat, String detailTemplate) {
    when(problemProperties.getDefaultDetailFormat()).thenReturn(defaultDetailFormat);

    TypeMismatchException ex = new TypeMismatchException("12", Integer.class);

    ResponseEntity<Object> response =
        handler.handleTypeMismatch(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
    assertEquals(detailTemplate.formatted(ex.getRequiredType()), problem.getDetail());
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
  }

  @Test
  void givenMethodValidationExceptionShouldGenerateProblem() {
    MethodValidationResult mockMethodValidationResult = mock(MethodValidationResult.class);
    MethodValidationException ex = new MethodValidationException(mockMethodValidationResult);

    ResponseEntity<Object> response =
        handler.handleMethodValidationException(
            ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mockWebRequest);

    assertInstanceOf(Problem.class, response.getBody());
    Problem problem = (Problem) response.getBody();
    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
  }

  private static class DummyController {
    public void someMethod(String id) {}
  }
}
