package io.github.malczuuu.problem4j.spring.web.advice;

import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.*;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

class AbstractAdviceTest {

  @Test
  void givenAbstractAdviceChildren_whenChecked_thenAreProperlyAnnotated() {
    Set<Class<?>> adviceClasses = findAbstractAdviceChildren();

    assertThat(adviceClasses).isNotEmpty();

    for (Class<?> clazz : adviceClasses) {
      assertThat(clazz.isAnnotationPresent(RestControllerAdvice.class))
          .as("%s must be annotated with @RestControllerAdvice", clazz.getName())
          .isTrue();

      Method handleMethod = getHandleMethod(clazz);

      ExceptionHandler annotation = handleMethod.getAnnotation(ExceptionHandler.class);
      assertThat(annotation)
          .as("%s.handle must be annotated with @ExceptionHandler", clazz.getName())
          .isNotNull();

      Class<? extends Throwable> genericType = getGenericExceptionType(clazz);

      assertThat(annotation.value())
          .as("%s.handle must declare @ExceptionHandler for %s", clazz.getName(), genericType)
          .containsExactly(genericType);
    }
  }

  private Set<Class<?>> findAbstractAdviceChildren() {
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(AbstractAdvice.class));

    return scanner.findCandidateComponents(AbstractAdviceTest.class.getPackageName()).stream()
        .map(
            bd -> {
              try {
                return Class.forName(bd.getBeanClassName());
              } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
              }
            })
        .filter(c -> !c.equals(AbstractAdvice.class))
        .collect(toSet());
  }

  private Method getHandleMethod(Class<?> clazz) {
    return java.util.Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> m.getName().equals("handle"))
        .findFirst()
        .orElseThrow(() -> new AssertionError(clazz + " does not declare handle()"));
  }

  private Class<? extends Throwable> getGenericExceptionType(Class<?> clazz) {
    Type genericSuperclass = clazz.getGenericSuperclass();
    if (genericSuperclass instanceof ParameterizedType parameterizedType) {
      Type actualType = parameterizedType.getActualTypeArguments()[0];
      if (actualType instanceof Class<?> c) {
        return cast(c);
      }
    }
    throw new IllegalStateException("cannot determine generic type for " + clazz);
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Throwable> cast(Class<?> clazz) {
    return (Class<? extends Throwable>) clazz;
  }
}
