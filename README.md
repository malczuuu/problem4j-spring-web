# Problem4J Spring Web

[![](https://jitpack.io/v/malczuuu/problem4j-spring-web.svg)](https://jitpack.io/#malczuuu/problem4j-spring-web)
[![Build Status](https://github.com/malczuuu/problem4j-spring-web/actions/workflows/gradle.yml/badge.svg)](https://github.com/malczuuu/problem4j-spring-web/actions/workflows/gradle.yml)
[![Weekly Build Status](https://github.com/malczuuu/problem4j-spring-web/actions/workflows/gradle-weekly.yml/badge.svg)](https://github.com/malczuuu/problem4j-spring-web/actions/workflows/gradle-weekly.yml)

> Part of [`problem4j`][problem4j] package of libraries.

Spring Web integration module for [`problem4j-core`][problem4j-core]. library that integrates the RFC Problem Details
model with exception handling in Spring Boot.

This library extends default `ResponseEntityExceptionHandler` with `ProblemResponseEntityExceptionHandler`, which maps
exceptions occurring in Spring controllers to `Problem` objects.

`problem4j-spring-boot-extension` is a library that extends the functionality of `problem4j`, enabling automatic
returning of `Problem` objects in HTTP responses instead of the default Spring Boot error responses. It is based on
extending `ResponseEntityExceptionHandler` and uses Spring Boot's autoconfiguration mechanism, making integration quick
and seamless.

## Table of Contents

- [Features](#features)
- [Usage](#usage)
- [Configuration](#configuration)
- [Extending behaviour via `ExceptionAdapter`](#extending-behaviour-via-exceptionadapter)
- [Deprecations](#deprecations)
- [Other Libraries](#other-libraries)

## Features

- ✅ Automatic mapping of exceptions to responses with `Problem` objects compliant with [RFC 7807][rfc7807].
- ✅ Error logging through a built-in `ExceptionLoggingAdapter`.
- ✅ Ability to create custom exception adapters (`ExceptionAdapter`) that can be registered as Spring beans to extend
  library behavior (e.g., storing errors in database).
- ✅ Simple configuration thanks to Spring Boot autoconfiguration.

## Usage

This library is available through [Jitpack][jitpack] repository. Add it along with repository in your dependency
manager.

1. Maven:
   ```xml
   <repositories>
       <repository>
           <id>jitpack.io</id>
           <url>https://jitpack.io</url>
       </repository>
   </repositories>
   <dependencies>
       <dependency>
           <groupId>com.github.malczuuu</groupId>
           <artifactId>problem4j-spring-web</artifactId>
           <version>${problem4j-spring-web.version}</version>
       </dependency>
   </dependencies>
   ```
2. Gradle (Groovy or Kotlin DSL):
   ```groovy
   repositories {
       maven { url = uri("https://jitpack.io") }
   }
   dependencies {
       implementation("com.github.malczuuu:problem4j-spring-web:${problem4j-spring-web.version}")
   }
   ```

## Configuration

Library can be configured with following properties.

* `problem4j.logging-enabled`. Allows to turn off default logging of controller exceptions.
* `problem4j.default-detail-format`. Specifies how default exception handling should print `defail` field of `Problem`
  model (`lowercase`, `capitalized` - default, `uppercase`).

## Extending behaviour via `ExceptionAdapter`

Custom adapters can be added by implementing `ExceptionAdapter` interface. **Note** that `ExceptionAdapters` are called
after constructing final `Problem` response object, but **before** returning HTTP response to the client so for
time-consuming extensions consider asynchronous processing.

```java

@Component
public class ExceptionStorageAdapter extends ExceptionAdapter {

    private final RestErrorStorage restErrorStorage;

    public ExceptionStorageAdapter(RestErrorStorage restErrorStorage) {
        this.restErrorStorage = restErrorStorage;
    }

    @Async
    @Override
    public void adapt(WebRequest request, Exception ex, Object body) {
        restErrorStorage.storeExceptionOccurrence(request, ex, body);
    }
}
```

## Deprecations

1. Previous versions of `problem4j-spring-web` used `@EnableProblem` annotation to include beans in your codebase.
   Versions `3.1+` use Spring Boot autoconfiguration and this annotation was marked as deprecated.

## Other Libraries

- [`problem4j-core`][problem4j-core] - Core library defining `Problem` model and `ProblemException`.
- [`problem4j-jackson`][problem4j-jackson] - Jackson module for serializing and deserializing `Problem` objects.

[jitpack]: https://jitpack.io/#malczuuu/problem4j-spring-web

[problem4j]: https://github.com/malczuuu/problem4j

[problem4j-core]: https://github.com/malczuuu/problem4j-core

[problem4j-jackson]: https://github.com/malczuuu/problem4j-jackson

[rfc7807]: https://datatracker.ietf.org/doc/html/rfc7807
