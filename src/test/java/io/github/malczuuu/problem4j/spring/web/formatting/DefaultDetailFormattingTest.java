package io.github.malczuuu.problem4j.spring.web.formatting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DefaultDetailFormattingTest {

  @Test
  void givenLowercaseFormat_whenFormatting_thenReturnsLowercase() {
    DefaultDetailFormatting formatting = new DefaultDetailFormatting("lowercase");

    String result = formatting.format("TeSt StrIng");

    assertThat(result).isEqualTo("test string");
  }

  @Test
  void givenUppercaseFormat_whenFormatting_thenReturnsUppercase() {
    DefaultDetailFormatting formatting = new DefaultDetailFormatting("uppercase");

    String result = formatting.format("TeSt StrIng");

    assertThat(result).isEqualTo("TEST STRING");
  }

  @Test
  void givenUnknownFormat_whenFormatting_thenReturnsUnchanged() {
    DefaultDetailFormatting formatting = new DefaultDetailFormatting("something-else");

    String result = formatting.format("TeSt StrIng");

    assertThat(result).isEqualTo("TeSt StrIng");
  }

  @Test
  void givenEmptyString_whenFormatting_thenReturnsEmpty() {
    DefaultDetailFormatting formatting = new DefaultDetailFormatting("uppercase");

    String result = formatting.format("");

    assertThat(result).isEqualTo("");
  }

  @Test
  void givenNullInput_whenFormatting_thenThrowsNpe() {
    DefaultDetailFormatting formatting = new DefaultDetailFormatting("uppercase");

    assertThrows(NullPointerException.class, () -> formatting.format(null));
  }
}
