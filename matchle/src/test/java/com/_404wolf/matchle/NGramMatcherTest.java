package com._404wolf.matchle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NGramMatcherTest {

  @Test
  void testPearlPlateMatch() {
    NGram key = NGram.from("pearl");
    NGram guess = NGram.from("plate");
    NGramMatcher matcher = NGramMatcher.of(key, guess);
    matcher.match();

    // Expected pattern:
    // p: CharMatch (index 0)
    // l: CharElsewhere (found in key)
    // a: CharMatch (index 2)
    // t: CharAbsent (not found)
    // e: CharElsewhere (found in key)

    assertEquals(
        "CharMatch, CharElsewhere, CharMatch, CharAbsent, CharElsewhere", matcher.toString());

    Filter filter = matcher.match();
    assertTrue(filter.test(key));
    assertFalse(filter.test(guess));
  }

  @Test
  void testRebusRouteMatch() {
    NGram key = NGram.from("rebus");
    NGram guess = NGram.from("route");
    NGramMatcher matcher = NGramMatcher.of(key, guess);
    matcher.match();

    // Expected pattern:
    // r: CharMatch (index 0)
    // o: CharAbsent (index 1)
    // u: CharElsewhere (from index 2)
    // t: CharAbsent (index 3)
    // e: CharElsewhere (from index 4)

    assertEquals(
        "CharMatch, CharAbsent, CharElsewhere, CharAbsent, CharElsewhere", matcher.toString());
  }

  @Test
  void testNullInputs() {
    assertThrows(NullPointerException.class, () -> NGramMatcher.of(null, NGram.from("test")));
    assertThrows(NullPointerException.class, () -> NGramMatcher.of(NGram.from("test"), null));
  }

  @Test
  void testDifferentLengths() {
    NGram key = NGram.from("pearl");
    NGram guess = NGram.from("pearls");
    NGramMatcher matcher = NGramMatcher.of(key, guess);
    Filter result = matcher.match();

    // Should return Filter.FALSE() for different lengths
    assertFalse(result.test(key));
  }
}
