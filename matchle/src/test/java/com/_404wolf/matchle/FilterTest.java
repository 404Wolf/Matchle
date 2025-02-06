package com._404wolf.matchle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class FilterTest {

  @Test
  void testFalse() {
    Filter falseFilter = Filter.FALSE;
    NGram testNGram = NGram.from("test");
    assertFalse(falseFilter.test(testNGram));
  }

  @Test
  void testFromWithNullPredicate() {
    assertThrows(NullPointerException.class, () -> Filter.from(null));
  }

  @Test
  void testFromWithValidPredicate() {
    Filter filter = Filter.from(ngram -> ngram.toString().length() > 3);
    NGram shortNGram = NGram.from("abc");
    NGram longNGram = NGram.from("abcd");

    assertFalse(filter.test(shortNGram));
    assertTrue(filter.test(longNGram));
  }

  @Test
  void testAndWithEmptyOptional() {
    Filter originalFilter = Filter.from(ngram -> ngram.toString().length() > 3);
    Filter combinedFilter = originalFilter.and(Optional.empty()); // should do nothing

    NGram shortNGram = NGram.from("abc");
    NGram longNGram = NGram.from("abcd");

    // Should behave same as original filter
    assertFalse(combinedFilter.test(shortNGram));
    assertTrue(combinedFilter.test(longNGram));
  }

  @Test
  void testAndWithPresentOptional() {
    Filter filter1 = Filter.from(ngram -> ngram.toString().length() > 3);
    Filter filter2 = Filter.from(ngram -> ngram.toString().startsWith("a"));
    Filter combinedFilter = filter1.and(Optional.of(filter2));

    NGram ngram1 = NGram.from("abc"); // short, starts with 'a'
    NGram ngram2 = NGram.from("abcd"); // long, starts with 'a'
    NGram ngram3 = NGram.from("bcd"); // short, doesn't start with 'a'
    NGram ngram4 = NGram.from("bcde"); // long, doesn't start with 'a'

    assertFalse(combinedFilter.test(ngram1)); // false because too short
    assertTrue(combinedFilter.test(ngram2)); // true because long and starts with 'a'
    assertFalse(combinedFilter.test(ngram3)); // false because short and doesn't start with 'a'
    assertFalse(combinedFilter.test(ngram4)); // false because doesn't start with 'a'
  }

  @Test
  void testMultipleAndOperations() {
    Filter filter1 = Filter.from(ngram -> ngram.toString().length() > 3);
    Filter filter2 = Filter.from(ngram -> ngram.toString().startsWith("a"));
    Filter filter3 = Filter.from(ngram -> ngram.toString().endsWith("z"));
    // should match greater than 3, starts with a, ends with z

    Filter combinedFilter = filter1.and(Optional.of(filter2)).and(Optional.of(filter3));

    NGram ngram1 = NGram.from("abcz"); // matches all conditions
    NGram ngram2 = NGram.from("abcd"); // long, starts with 'a', doesn't end with 'z'
    NGram ngram3 = NGram.from("bcz"); // short, ends with 'z'
    NGram ngram4 = NGram.from("bcd"); // doesn't match any condition

    assertTrue(combinedFilter.test(ngram1));
    assertFalse(combinedFilter.test(ngram2));
    assertFalse(combinedFilter.test(ngram3));
    assertFalse(combinedFilter.test(ngram4));
  }
}
