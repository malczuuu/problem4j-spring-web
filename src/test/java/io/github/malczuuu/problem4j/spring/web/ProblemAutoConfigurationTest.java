package io.github.malczuuu.problem4j.spring.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = {JacksonAutoConfiguration.class, ProblemAutoConfiguration.class})
@ExtendWith(SpringExtension.class)
class ProblemAutoConfigurationTest {

  @Test
  void contextLoads() {}
}
