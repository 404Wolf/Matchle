package com._404wolf.matchle;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The {@code Corpus} class represents a collection of n-grams that form a dictionary for
 * match-related applications. It ensures that all words in the dictionary have the same length.
 */
public final class Corpus implements Iterable<NGram> {
  private final Set<NGram> corpus;

  private Corpus(Set<NGram> corpus) {
    assert Objects.nonNull(corpus) : "Corpus cannot be null";

    this.corpus = Collections.unmodifiableSet(corpus);
  }

  /**
   * Returns a copy of the internal corpus.
   *
   * @return a copy of the internal corpus
   */
  Set<NGram> corpus() {
    return Set.copyOf(corpus);
  }

  /**
   * Returns the common size of the n-grams in the corpus
   *
   * @return the common size of the n-grams in the corpus
   */
  int wordSize() {
    return corpus.iterator().next().size();
  }

  @Override
  public java.util.Iterator<NGram> iterator() {
    return corpus.iterator();
  }

  /**
   * A builder to create Corpus objects.
   *
   * <h2>Example</h2>
   *
   * <pre>
   *
   * Corpus corpus = new Corpus.Builder()
   *     .addNGram(new NGram("word"))
   *     .addNGram(new NGram("test"))
   *     .build();
   * </pre>
   *
   * @return A new {@code Builder} object that can be used to create a new {@code Corpus} object.
   */
  public static final class Builder {
    private final Set<NGram> ngrams;

    private Builder(Set<NGram> ngrams) {
      this.ngrams = ngrams;
    }

    public static final Builder EMPTY() {
      return new Builder(new HashSet<>());
    }

    /**
     * Filters the n-grams in the corpus using the given filter, providing a new Builder only with
     * passing n-grams.
     *
     * @returns a builder with the n-grams that are consistent with the filter
     */
    public Builder filter(Filter filter) {
      Objects.requireNonNull(filter, "filter cannot be null");

      Set<NGram> filteredNgrams = new HashSet<>();
      ngrams.stream().filter(filter::test).forEach(filteredNgrams::add);

      return Builder.of(new Corpus(filteredNgrams));
    }
    ;

    /**
     * Adds an n-gram to the corpus.
     *
     * @param nGram the n-gram to add
     * @throws NullPointerException if the n-gram is null
     * @return this Builder instance for method chaining
     */
    public Builder add(NGram nGram) throws NullPointerException {
      Objects.requireNonNull(nGram, "nGram cannot be null");
      ngrams.add(nGram);
      return this;
    }

    /**
     * Adds all n-grams from the given collection to the corpus.
     *
     * @param nGrams the collection of n-grams to add
     * @throws NullPointerException if the collection is null
     * @return this Builder instance for method chaining
     */
    public Builder addAll(Collection<NGram> nGrams) throws NullPointerException {
      Objects.requireNonNull(nGrams, "nGrams collection cannot be null");
      nGrams.stream().filter(Objects::nonNull).forEach(ngrams::add);
      return this;
    }

    /**
     * Checks if all n-grams in the corpus have the specified word size.
     *
     * @param wordSize the size to check against
     * @return true if all n-grams have the specified word size, false otherwise
     */
    public boolean isConsistent(Integer wordSize) {
      return ngrams.stream().allMatch(ngram -> ngram.size() == wordSize);
    }

    /**
     * Returns a copy of a new Corpus, or null if not all n-grams are the same size.
     *
     * @return a copy of new Corpus, or null if not all n-grams are the same size.
     */
    public Corpus build() {
      return Optional.of(ngrams)
          .filter(list -> list.stream().map(NGram::size).distinct().count() <= 1)
          .map(list -> new Corpus(new HashSet<>(list)))
          .orElse(null);
    }

    /**
     * Obtain a builder with n-grams that are the same as a given Corpus's
     *
     * @return A builder with n-grams that are the same as a given Corpus's.
     * @param corpus The Corpus you want a Builder for.
     */
    public static final Builder of(Corpus corpus) {
      Builder builder = new Builder(new HashSet<>());
      corpus.forEach(builder::add);
      return builder;
    }
  }

  /**
   * Figure out how many valid n-grams there are in the corpus that pass filter tests.
   *
   * <p>For example, a Corpus containing “route”, “rebus”, “redux”, and “hello” would return 2
   * because “rebus” and “redux” are the two n-grams consistent with the ﬁlter.
   *
   * @return The number of n-grams consistent with the filter.
   */
  public long size(Filter filter) {
    return corpus.stream().filter(filter::test).count();
  }

  /**
   * The size of the corpus that is consistent with the ﬁlter that matches the key and the guess
   * n-grams
   */
  public long score(NGram key, NGram guess) {
    if (corpus.stream().count() == 0) {
      throw new IllegalStateException("Corpus is empty and cannot be scored");
    } else {
      NGramMatcher matcher = NGramMatcher.of(key, guess);
      return size(matcher.match());
    }
  }
}
