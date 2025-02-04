package com._404wolf.matchle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class NGramTest {

  @Test
  void testFromList() {
    List<Character> chars = Arrays.asList('t', 'e', 's', 't');
    NGram ngram = NGram.from(chars);
    assertEquals(4, ngram.size());
    assertEquals('t', ngram.get(0));
    assertEquals('e', ngram.get(1));
    assertEquals('s', ngram.get(2));
    assertEquals('t', ngram.get(3));
  }

  @Test
  void testFromString() {
    NGram ngram = NGram.from("test");
    assertEquals(4, ngram.size());
    assertEquals('t', ngram.get(0));
    assertEquals('e', ngram.get(1));
    assertEquals('s', ngram.get(2));
    assertEquals('t', ngram.get(3));
  }

  @Test
  void testFromNullString() {
    assertThrows(NullPointerException.class, () -> NGram.from((String) null));
  }

  @Test
  void testFromListWithNullCharacter() {
    List<Character> chars = Arrays.asList('t', 'e', null, 't');
    assertThrows(IllegalArgumentException.class, () -> NGram.from(chars));
  }

  @Test
  void testGet() {
    NGram ngram = NGram.from("test");
    assertEquals('t', ngram.get(0));
    assertEquals('e', ngram.get(1));
    assertEquals('s', ngram.get(2));
    assertEquals('t', ngram.get(3));
  }

  @Test
  void testGetOutOfBounds() {
    NGram ngram = NGram.from("test");
    assertThrows(IndexOutOfBoundsException.class, () -> ngram.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> ngram.get(4));
  }

  @Test
  void testSize() {
    NGram ngram = NGram.from("test");
    assertEquals(4, ngram.size());
  }

  @Test
  void testMatches() {
    NGram ngram = NGram.from("test");
    assertTrue(ngram.matches(new NGram.IndexedCharacter(0, 't')));
    assertTrue(ngram.matches(new NGram.IndexedCharacter(1, 'e')));
    assertFalse(ngram.matches(new NGram.IndexedCharacter(0, 'e')));
    assertFalse(ngram.matches(new NGram.IndexedCharacter(4, 't')));
  }

  @Test
  void testContains() {
    NGram ngram = NGram.from("test");
    assertTrue(ngram.contains('t'));
    assertTrue(ngram.contains('e'));
    assertTrue(ngram.contains('s'));
    assertFalse(ngram.contains('x'));
  }

  @Test
  void testContainsElsewhere() {
    NGram ngram = NGram.from("tester");
    assertTrue(ngram.containsElsewhere(new NGram.IndexedCharacter(0, 't')));
    assertFalse(ngram.containsElsewhere(new NGram.IndexedCharacter(2, 's')));
  }

  @Test
  void testStream() {
    NGram ngram = NGram.from("test");
    List<NGram.IndexedCharacter> chars = ngram.stream().collect(Collectors.toList());
    assertEquals(4, chars.size());
    assertEquals(new NGram.IndexedCharacter(0, 't'), chars.get(0));
    assertEquals(new NGram.IndexedCharacter(1, 'e'), chars.get(1));
    assertEquals(new NGram.IndexedCharacter(2, 's'), chars.get(2));
    assertEquals(new NGram.IndexedCharacter(3, 't'), chars.get(3));
  }

  @Test
  void testIterator() {
    NGram ngram = NGram.from("test");
    Iterator<NGram.IndexedCharacter> iterator = ngram.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(new NGram.IndexedCharacter(0, 't'), iterator.next());
    assertEquals(new NGram.IndexedCharacter(1, 'e'), iterator.next());
    assertEquals(new NGram.IndexedCharacter(2, 's'), iterator.next());
    assertEquals(new NGram.IndexedCharacter(3, 't'), iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  void testEquals() {
    NGram ngram1 = NGram.from("test");
    NGram ngram2 = NGram.from("test");
    NGram ngram3 = NGram.from("tset");

    assertEquals(ngram1, ngram2);
    assertNotEquals(ngram1, ngram3);
  }

  @Test
  void testHashCode() {
    NGram ngram1 = NGram.from("test");
    NGram ngram2 = NGram.from("test");
    NGram ngram3 = NGram.from("tset");

    assertEquals(ngram1.hashCode(), ngram2.hashCode());
    assertNotEquals(ngram1.hashCode(), ngram3.hashCode());
  }
}
