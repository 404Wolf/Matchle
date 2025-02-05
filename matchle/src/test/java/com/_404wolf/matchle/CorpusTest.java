package com._404wolf.matchle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CorpusTest {

  @Test
  void testCorpusCreation() {
    Corpus corpus = Corpus.Builder.EMPTY().add(NGram.from("word")).add(NGram.from("test")).build();

    assertNotNull(corpus);
    assertEquals(2, corpus.corpus().size());
  }

  @Test
  void testCorpusWithDifferentSizedNGrams() {
    Corpus corpus =
        Corpus.Builder.EMPTY()
            .add(NGram.from("word"))
            .add(NGram.from("test"))
            .add(NGram.from("longword"))
            .build();

    assertNull(corpus);
  }

  @Test
  void testCorpusCopy() {
    Corpus corpus = Corpus.Builder.EMPTY().add(NGram.from("word")).add(NGram.from("test")).build();

    Set<NGram> copiedCorpus = corpus.corpus();
    assertEquals(corpus.corpus(), copiedCorpus);
    assertNotSame(corpus.corpus(), copiedCorpus);
  }

  @Test
  void testWordSize() {
    Corpus corpus = Corpus.Builder.EMPTY().add(NGram.from("word")).add(NGram.from("test")).build();

    assertEquals(4, corpus.wordSize());
  }

  @Test
  void testIterator() {
    Corpus corpus = Corpus.Builder.EMPTY().add(NGram.from("word")).add(NGram.from("test")).build();

    Iterator<NGram> iterator = corpus.iterator();
    assertTrue(iterator.hasNext());
    assertNotNull(iterator.next());
    assertTrue(iterator.hasNext());
    assertNotNull(iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  void testBuilderAddAll() {
    Set<NGram> nGrams = new HashSet<>(Arrays.asList(NGram.from("word"), NGram.from("test")));

    Corpus corpus = Corpus.Builder.EMPTY().addAll(nGrams).build();

    assertNotNull(corpus);
    assertEquals(2, corpus.corpus().size());
  }

  @Test
  void testAddNullNGram() {
    assertThrows(NullPointerException.class, () -> Corpus.Builder.EMPTY().add(null));
  }

  @Test
  void testBuilderOf() {
    Corpus originalCorpus =
        Corpus.Builder.EMPTY().add(NGram.from("word")).add(NGram.from("test")).build();

    Corpus.Builder builder = Corpus.Builder.of(originalCorpus);
    Corpus newCorpus = builder.build();

    assertNotNull(newCorpus);
    assertEquals(originalCorpus.corpus(), newCorpus.corpus());
  }

  @Test
  void testEmptyBuilder() {
    Corpus corpus = Corpus.Builder.EMPTY().build();
    assertNotNull(corpus);
  }
}
