package io.github.malczuuu.problem4j.spring.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * @deprecated Since 3.1.0, this library is using Spring Boot autoconfiguration. There's no need to
 *     use {@code @EnableProblem} in your application.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ProblemAutoConfiguration.class)
public @interface EnableProblem {}
