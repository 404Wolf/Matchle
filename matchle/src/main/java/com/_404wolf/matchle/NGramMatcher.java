package com._404wolf.matchle;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class NGramMatcher {
  enum MatchReportStatus {
    CharMatch,
    CharElsewhere,
    CharAbsent
  }

  private final NGram key;
  private final NGram guess;
  private final boolean[] matchedKeys;
  private final boolean[] matchedGuesses;
  private final List<MatchReport> reports = new LinkedList<>();

  private boolean matched = false;

  private record MatchReport(IndexedCharacter indexedCharacter, MatchReportStatus status) {
    MatchReport(int index, char character, MatchReportStatus status) {
      this(new IndexedCharacter(index, character), status);
    }
  }

  private NGramMatcher(NGram key, NGram guess) {
    assert Objects.nonNull(key) : "key cannot be null";
    assert Objects.nonNull(guess) : "guess cannot be null";

    this.key = key;
    this.guess = guess;

    matchedKeys = new boolean[guess.size()];
    matchedGuesses = new boolean[guess.size()];
  }

  public static NGramMatcher of(NGram key, NGram guess) {
    Objects.requireNonNull(key, "key cannot be null");
    Objects.requireNonNull(guess, "guess cannot be null");

    return new NGramMatcher(key, guess);
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
    if (matched) throw new IllegalArgumentException("can't match multiple times");

    try {
      return Optional.of(Filter.FALSE)
          .filter(b -> key.size() == guess.size()) // Check if the n-grams have the same length
          .map(b -> buildMatchFilter()) // Build the match filter if conditions met
          .orElse(Filter.FALSE); // Return FALSE if lengths don't match
    } finally {
      System.out.println(this);
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
  private Filter buildMatchFilter() {
    matchSamePosition().matchDifferentPositions().matchAbsentCharacters();

    return reports.stream()
        .map(
            report ->
                switch (report.status()) {
                  case CharMatch -> Filter.from(ngram -> ngram.matches(report.indexedCharacter()));
                  case CharElsewhere ->
                      Filter.from(ngram -> ngram.containsElsewhere(report.indexedCharacter()));
                  case CharAbsent ->
                      Filter.from(ngram -> !ngram.contains(report.indexedCharacter().character()));
                })
        // ^ The function that we add to filters list is based on the status of the
        // report
        .reduce((acc, cur) -> acc.and(Optional.of(cur)))
        // reduces to an optional in case reduce fails (e.g. no items in list)
        .orElse(Filter.FALSE); // fall back to false
  }

  private IntStream guessIndexIntStream() {
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
    assert guessIndexIntStream().mapToObj(i -> matchedKeys[i]).allMatch(b -> !b)
        : "Matched keys should all be false";

    guessIndexIntStream()
        .filter(i -> guess.get(i) == key.get(i)) // only keep exact matches
        .forEach(
            i -> {
              MatchReport report = new MatchReport(i, key.get(i), MatchReportStatus.CharMatch);
              reports.add(report);

              matchedKeys[i] = true;
              matchedGuesses[i] = true;
            });

    return this;
  }

  /**
   * Process all indeces where there are matches in different positions. For each character, if it
   * hasn't been matched yet, if that letter shows up somewhere else in the key, mark the index in
   */
  private NGramMatcher matchDifferentPositions() {
    guessIndexIntStream()
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
                        // we matched the "elsewhere" char we assigned matchedGuesses[i] to
                        matchedKeys[j] = true;
                        matchedGuesses[i] = true; // we matched the current guess

                        MatchReport report =
                            new MatchReport(i, guess.get(i), MatchReportStatus.CharElsewhere);
                        reports.add(report);
                      });
            });

    return this;
  }

  /**
   * Process all indeces where the guess totally failed to match with any characters in the key. All
   * that must be done here is report the character at i as absent.
   */
  private NGramMatcher matchAbsentCharacters() {
    guessIndexIntStream()
        .filter(i -> !matchedGuesses[i])
        .forEach(
            i -> {
              MatchReport report = new MatchReport(i, guess.get(i), MatchReportStatus.CharAbsent);

              reports.add(report);
            });

    return this;
  }
}
