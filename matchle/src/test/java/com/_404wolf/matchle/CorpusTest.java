package com._404wolf.matchle;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class CorpusTest {

  @Test
  void testCorpusCreation() {
    Corpus corpus = new Corpus.Builder()
        .add(NGram.from("word"))
        .add(NGram.from("test"))
        .build();

    assertNotNull(corpus);
    assertEquals(2, corpus.getCorpus().size());
  }

  @Test
  void testCorpusWithDifferentSizedNGrams() {
    Corpus corpus = new Corpus.Builder()
        .add(NGram.from("word"))
        .add(NGram.from("test"))
        .add(NGram.from("longword"))
        .build();

    assertNull(corpus);
  }

  @Test
  void testCorpusCopy() {
    Corpus corpus = new Corpus.Builder()
        .add(NGram.from("word"))
        .add(NGram.from("test"))
        .build();

    Set<NGram> copiedCorpus = corpus.corpus();
    assertEquals(corpus.getCorpus(), copiedCorpus);
    assertNotSame(corpus.getCorpus(), copiedCorpus);
  }

  @Test
  void testWordSize() {
    Corpus corpus = new Corpus.Builder()
        .add(NGram.from("word"))
        .add(NGram.from("test"))
        .build();

    assertEquals(4, corpus.wordSize());
  }

  @Test
  void testIterator() {
    Corpus corpus = new Corpus.Builder()
        .add(NGram.from("word"))
        .add(NGram.from("test"))
        .build();

    Iterator<NGram> iterator = corpus.iterator();
    assertTrue(iterator.hasNext());
    assertNotNull(iterator.next());
    assertTrue(iterator.hasNext());
    assertNotNull(iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  void testBuilderAddAll() {
    Set<NGram> nGrams = new HashSet<>(Arrays.asList(
        NGram.from("word"),
        NGram.from("test")));

    Corpus corpus = new Corpus.Builder()
        .addAll(nGrams)
        .build();

    assertNotNull(corpus);
    assertEquals(2, corpus.getCorpus().size());
  }

  @Test
  void testBuilderAddAllWithNull() {
    assertThrows(NullPointerException.class, () -> {
      new Corpus.Builder().addAll(null);
    });
  }

  @Test
  void testBuilderAddAllWithNullElement() {
    Set<NGram> nGrams = new HashSet<>(Arrays.asList(
        NGram.from("word"),
        null));

    assertThrows(NullPointerException.class, () -> {
      new Corpus.Builder().addAll(nGrams);
    });
  }

  @Test
  void testBuilderOf() {
    Corpus originalCorpus = new Corpus.Builder()
        .add(NGram.from("word"))
        .add(NGram.from("test"))
        .build();

    Corpus.Builder builder = Corpus.Builder.of(originalCorpus);
    Corpus newCorpus = builder.build();

    assertNotNull(newCorpus);
    assertEquals(originalCorpus.getCorpus(), newCorpus.getCorpus());
  }

  @Test
  void testEmptyBuilder() {
    Corpus corpus = Corpus.Builder.EMPTY.build();
    assertNull(corpus);
  }
}
