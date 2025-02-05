package com._404wolf.matchle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class FilterTest {

  @Test
  void testFALSE() {
    Filter falseFilter = Filter.FALSE();
    NGram testNGram = NGram.from("test"); // Assuming NGram constructor exists
    assertFalse(falseFilter.test(testNGram));
  }

  @Test
  void testFromNullPredicate() {
    assertThrows(NullPointerException.class, () -> Filter.from(null));
  }

  @Test
  void testFromValidPredicate() {
    Filter filter = Filter.from(ngram -> true);
    assertNotNull(filter);
  }

  @Test
  void testAndWithEmptyOptional() {
    Filter originalFilter = Filter.from(ngram -> true);
    Filter combined = originalFilter.and(Optional.empty());
    NGram testNGram = NGram.from("test");

    assertTrue(combined.test(testNGram));
  }

  @Test
  void testAndWithPresentOptional() {
    Filter filter1 = Filter.from(ngram -> true);
    Filter filter2 = Filter.from(ngram -> false);
    Filter combined = filter1.and(Optional.of(filter2));
    NGram testNGram = NGram.from("test");

    assertFalse(combined.test(testNGram));
  }

  @Test
  void testComplexAndOperation() {
    Filter filter1 = Filter.from(ngram -> ngram.toString().length() > 2);
    Filter filter2 = Filter.from(ngram -> ngram.toString().startsWith("t"));
    Filter combined = filter1.and(Optional.of(filter2));

    NGram validNGram = NGram.from("test");
    NGram invalidNGram1 = NGram.from("ab"); // too short
    NGram invalidNGram2 = NGram.from("abcd"); // doesn't start with 't'

    assertTrue(combined.test(validNGram));
    assertFalse(combined.test(invalidNGram1));
    assertFalse(combined.test(invalidNGram2));
  }
}
