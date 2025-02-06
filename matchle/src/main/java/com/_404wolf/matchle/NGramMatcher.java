package com._404wolf.matchle;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class NGramMatcher {
  enum MatchReportStatus {
    CharMatch,
    CharElsewhere,
    CharAbsent
  }

  private record MatchReport(IndexedCharacter indexedCharacter, MatchReportStatus status) {}

  public static NGramMatcher of(NGram key, NGram guess) {
    Objects.requireNonNull(key, "key cannot be null");
    Objects.requireNonNull(guess, "guess cannot be null");

    return new NGramMatcher(key, guess);
  }

  private final NGram key;
  private final NGram guess;
  private final boolean[] matchedKeys;
  private final boolean[] matchedGuesses;
  private final List<MatchReport> reports = new LinkedList<>();

  private boolean matched = false;
  private boolean matchedSamePosition = false;
  private boolean matchedDifferentPositions = false;
  private boolean matchedAbsentCharacters = false;

  private NGramMatcher(NGram key, NGram guess) {
    this.key = key;
    this.guess = guess;

    matchedKeys = new boolean[guess.size()];
    matchedGuesses = new boolean[guess.size()];
  }

  @Override
  public String toString() {
    reports.sort(Comparator.comparingInt(report -> report.indexedCharacter().index()));
    return reports.stream()
        .map(report -> report.status().toString())
        .collect(Collectors.joining(", "));
  }

  /**
   * Generates a Filter that represents the matching pattern between the key and guess. The
   * resulting filter can be used to test other n-grams for the same pattern.
   *
   * @return a Filter that represents the matching pattern, or Filter.FALSE if the n-grams have
   *     different lengths
   */
  public Filter match() {
    Optional.of(matched)
        .filter(isMatched -> !isMatched) // if we have NOT matched, "keep it" and then don't throw
        .orElseThrow(() -> new IllegalArgumentException("can't match multiple times"));

    try {
      return Optional.of(Filter.FALSE())
          .filter(b -> key.size() == guess.size()) // Check if the n-grams have the same length
          .map(b -> buildMatchFilter()) // Build the match filter if conditions met
          .orElse(Filter.FALSE()); // Return FALSE if lengths don't match
    } finally {
      matched = true;
    }
  }

  /**
   * Builds a predicate that represents the matching pattern between the key and guess. The matching
   * algorithm works in three phases: 1. Matches identical characters in the same positions 2.
   * Matches identical characters in different positions 3. Identifies absent characters
   *
   * @return a Predicate that combines all matching conditions with logical AND
   */
  private static final EnumMap<MatchReportStatus, Function<MatchReport, Filter>> FILTER_STRATEGIES =
      new EnumMap<>(
          Map.of(
              MatchReportStatus.CharMatch,
              report -> Filter.from(ngram -> ngram.matches(report.indexedCharacter())),
              MatchReportStatus.CharElsewhere,
              report -> Filter.from(ngram -> ngram.containsElsewhere(report.indexedCharacter())),
              MatchReportStatus.CharAbsent,
              report ->
                  Filter.from(ngram -> !ngram.contains(report.indexedCharacter().character()))));

  private Filter buildMatchFilter() {
    // At this point we should not have matched already
    assert !matched : "Match filter already built";
    assert !matchedSamePosition : "Same positions already matched";
    assert !matchedDifferentPositions : "Different positions already matched";
    assert !matchedAbsentCharacters : "Absent characters already matched";

    matchSamePosition().matchDifferentPositions().matchAbsentCharacters();

    return reports.stream()
        .map(report -> FILTER_STRATEGIES.get(report.status()).apply(report))
        // ^ The function that we add to filters list is based on the status of the
        // report
        .reduce((acc, cur) -> acc.and(Optional.of(cur)))
        // reduces to an optional in case reduce fails (e.g. no items in list)
        .orElse(Filter.FALSE()); // fall back to false
  }

  private IntStream guessIndexStream() {
    return IntStream.range(0, guess.size());
  }

  /**
   * Process all indeces where there are matches. Wherever there is a match, eliminate that index
   * from futher consideration by matching it, add to the reports list, and then add a check at that
   * index for the filters.
   */
  private NGramMatcher matchSamePosition() {
    // We shouldn't have matched already, and the arrays should start as emtpy (all
    // false)
    assert !matched : "Match filter already built";
    assert !matchedSamePosition : "Same positions already matched";
    assert guessIndexStream().mapToObj(i -> matchedKeys[i]).allMatch(b -> !b)
        : "Matched keys should all be false";

    guessIndexStream()
        .filter(i -> guess.get(i) == key.get(i)) // only keep exact matches
        .forEach(
            i -> {
              reports.add(
                  new MatchReport(
                      new IndexedCharacter(i, key.get(i)), MatchReportStatus.CharMatch));

              matchedKeys[i] = true;
              matchedGuesses[i] = true;
            });

    matchedSamePosition = true;
    return this;
  }

  /**
   * Process all indeces where there are matches in different positions. For each character, if it
   * hasn't been matched yet, if that letter shows up somewhere else in the key, mark the index in
   */
  private NGramMatcher matchDifferentPositions() {
    assert matchedSamePosition : "Must match same positions first";
    assert !matchedDifferentPositions : "Different positions already matched";

    guessIndexStream()
        .filter(i -> !matchedGuesses[i]) // make sure we haven't matched this already
        .forEach(
            i -> {
              // For each char in the guess check if it exists elsewhere
              IntStream.range(0, key.size())
                  .filter(
                      j -> j != i) // don't count this index, it's our own index (not "elsewhere")
                  .filter(j -> key.get(j) == guess.get(i)) // chars match (and it's not our index)
                  .findFirst() // get the first char that matched to "use" as the matchpoint
                  .ifPresent( // if there was a match, handle it
                      j -> {
                        matchedKeys[j] =
                            true; // we matched the "elsewhere" char we assigned matchedGuesses[i]
                                  // to
                        matchedGuesses[i] = true; // we matched the current guess
                        reports.add(
                            new MatchReport(
                                new IndexedCharacter(i, guess.get(i)),
                                MatchReportStatus.CharElsewhere));
                      });
            });

    matchedDifferentPositions = true;
    return this;
  }

  /**
   * Process all indeces where the guess totally failed to match with any characters in the key. All
   * that must be done here is report the character at i as absent.
   */
  private NGramMatcher matchAbsentCharacters() {
    assert matchedSamePosition : "Must match same positions first";
    assert matchedDifferentPositions : "Must match different positions first";
    assert !matchedAbsentCharacters : "Absent characters already matched";

    guessIndexStream()
        .filter(i -> !matchedGuesses[i])
        .forEach(
            i ->
                reports.add(
                    new MatchReport(
                        new IndexedCharacter(i, guess.get(i)), MatchReportStatus.CharAbsent)));

    matchedAbsentCharacters = true;
    return this;
  }
}
