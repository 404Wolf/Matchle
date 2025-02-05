package com._404wolf.matchle;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a predicate for filtering n-grams in a corpus. This class encapsulates filtering logic
 * for n-grams and provides methods for combining filters using logical operations.
 *
 * <p>A Filter instance is immutable and thread-safe. It can be created using the static factory
 * method {@code from()} with a predicate, and includes methods for combining filters through
 * logical operations.
 */
public final class Filter {
  /**
   * Returns a Filter that always evaluates to false for any NGram.
   *
   * @return a Filter that always returns false
   */
  public static final Filter FALSE() {
    return Filter.from((n) -> false);
  }

  /**
   * Creates a new Filter from the given predicate.
   *
   * @param predicate the predicate to use for filtering
   * @return a new Filter instance
   * @throws NullPointerException if the predicate is null
   */
  public static Filter from(Predicate<NGram> predicate) {
    Objects.requireNonNull(predicate);
    return new Filter(predicate);
  }

  /** The predicate used for filtering NGrams */
  private final Predicate<NGram> predicate;

  /**
   * Private constructor to create a new Filter with the given predicate.
   *
   * @param predicate the predicate to use for filtering
   */
  private Filter(Predicate<NGram> predicate) {
    this.predicate = predicate;
  }

  public boolean test(NGram ngram) {
    return predicate.test(ngram);
  }

  /**
   * Combines this filter with another optional filter using logical AND. If the other filter is
   * empty, returns a filter equivalent to this one.
   *
   * @param other the optional filter to combine with this one
   * @return a new Filter representing the logical AND of both filters
   */
  public Filter and(Optional<Filter> other) {
    return Filter.from(this.predicate.and(other.orElse(Filter.from((n) -> true)).predicate));
  }
}
