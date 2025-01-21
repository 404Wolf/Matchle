package com._404wolf.longestHigherSuffix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class HighListSuffixTest {
  @Test
  public void longestHigherSuffixTest() {
    // Test case 1
    List<Integer> a1 = Arrays.asList(0, 2, 4);
    List<Integer> b1 = Arrays.asList(1, 2, 3);
    List<Integer> expected1 = Arrays.asList(2, 4);
    assertEquals(expected1, HighListSuffix.longestHigherSuffix(a1, b1, Comparator.naturalOrder()));

    // Test case 2
    List<Integer> a2 = Arrays.asList(1, 2);
    List<Integer> b2 = Arrays.asList(2, 1);
    List<Integer> expected2 = Arrays.asList(2);
    assertEquals(expected2, HighListSuffix.longestHigherSuffix(a2, b2, Comparator.naturalOrder()));

    // Test case 3
    List<Integer> a3 = Arrays.asList(2, 4);
    List<Integer> b3 = Arrays.asList(1, 3, 2, 4);
    List<Integer> expected3 = Arrays.asList(2, 4);
    assertEquals(expected3, HighListSuffix.longestHigherSuffix(a3, b3, Comparator.naturalOrder()));

    // Test case 4
    List<Integer> a4 = Arrays.asList(1, 2, 3, 4);
    List<Integer> b4 = Arrays.asList(1, 2, 4);
    List<Integer> expected4 = Arrays.asList(2, 3, 4);
    assertEquals(expected4, HighListSuffix.longestHigherSuffix(a4, b4, Comparator.naturalOrder()));

    // Test case 5
    List<Integer> a5 = Arrays.asList(2, 1);
    List<Integer> b5 = Arrays.asList(1, 2, 3);
    List<Integer> expected5 = Arrays.asList();
    assertEquals(expected5, HighListSuffix.longestHigherSuffix(a5, b5, Comparator.naturalOrder()));

    // Test case 6
    List<Integer> a6 = Arrays.asList(1, 3, 2, 4);
    List<Integer> b6 = Arrays.asList(1, 2, 3, 4);
    List<Integer> expected6 = Arrays.asList(4);
    assertEquals(expected6, HighListSuffix.longestHigherSuffix(a6, b6, Comparator.naturalOrder()));
  }
}
