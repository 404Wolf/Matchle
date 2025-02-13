package com._404wolf.matchle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.ToLongFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CorpusTest {
  private static Corpus exampleCorpus;

  @BeforeAll
  static void setUp() {
    exampleCorpus =
        Corpus.Builder.EMPTY()
            .add(NGram.from("rebus"))
            .add(NGram.from("redux"))
            .add(NGram.from("route"))
            .add(NGram.from("hello"))
            .build();
  }

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

  @Test
  void testCorpusSize() {
    // Test with a filter that matches words starting with "re"
    Filter startsWithRe = Filter.from(ngram -> ngram.toString().startsWith("re"));
    assertEquals(2, exampleCorpus.size(startsWithRe));

    // Test with a filter that matches nothing
    Filter matchesNothing = Filter.from(ngram -> false);
    assertEquals(0, exampleCorpus.size(matchesNothing));

    // Test with a filter that matches everything
    Filter matchesEverything = Filter.from(ngram -> true);
    assertEquals(4, exampleCorpus.size(matchesEverything));

    // Test with a specific pattern filter
    Filter containsE = Filter.from(ngram -> ngram.toString().contains("e"));
    assertEquals(4, exampleCorpus.size(containsE));
  }

  @Test
  void testCorpusScore() {
    Corpus corpus =
        Corpus.Builder.EMPTY()
            .add(NGram.from("foo"))
            .add(NGram.from("bar"))
            .add(NGram.from("buz"))
            .build();

    // Now test the score
    assertEquals(1, corpus.score(NGram.from("foo"), NGram.from("foo")));

    // If the corpus is empty we should get a IllegalStateException when trying
    // to score
    assertThrows(
        IllegalStateException.class,
        () -> Corpus.Builder.EMPTY().build().score(NGram.from(""), NGram.from("")));
  }

  @Test
  void testScore() {
    NGram guess = NGram.from("route");

    assertEquals(2, exampleCorpus.score(NGram.from("rebus"), guess));
    assertEquals(2, exampleCorpus.score(NGram.from("redux"), guess));
    assertEquals(1, exampleCorpus.score(NGram.from("route"), guess));
    assertEquals(1, exampleCorpus.score(NGram.from("hello"), guess));
  }

  @Test
  void testScoreWithEmptyCorpus() {
    Corpus emptyCorpus = Corpus.Builder.EMPTY().build();
    assertThrows(
        IllegalStateException.class,
        () -> emptyCorpus.score(NGram.from("test"), NGram.from("test")));
  }

  @Test
  void testScoreWorstCase() {
    assertEquals(1, exampleCorpus.scoreWorstCase(NGram.from("route")));
  }

  @Test
  void testScoreAverageCase() {
    assertEquals(6, exampleCorpus.scoreAverageCase(NGram.from("route")));
  }

  @Test
  void testBestWorstCaseGuess() {
    NGram bestGuess = exampleCorpus.bestWorstCaseGuess();
    assertNotNull(bestGuess);
    assertTrue(exampleCorpus.corpus().contains(bestGuess));
  }

  @Test
  void testBestAverageCaseGuess() {
    NGram bestGuess = exampleCorpus.bestAverageCaseGuess();
    assertNotNull(bestGuess);
    assertTrue(exampleCorpus.corpus().contains(bestGuess));
  }

  @Test
  void testBestGuessWithCustomCriterion() {
    ToLongFunction<NGram> criterion = ngram -> 1L; // Simple criterion that always returns 1
    NGram bestGuess = exampleCorpus.bestGuess(criterion);
    assertNotNull(bestGuess);
    assertTrue(exampleCorpus.corpus().contains(bestGuess));
  }

  @Test
  void testBestGuessWithNullCriterion() {
    assertThrows(NullPointerException.class, () -> exampleCorpus.bestGuess(null));
  }
}
