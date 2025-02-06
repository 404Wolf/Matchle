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
    return Optional.of(!matched)
        .filter(b -> b)
        .filter(ng -> key.size() == guess.size()) // return a false filter if sizes mismatch
        .map(b -> buildMatchFilter())
        .orElse(Filter.FALSE());
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
        .peek(filter -> System.out.println(filter))
        .reduce((f1, f2) -> f1.and(Optional.of(f2)))
        .orElse(Filter.FALSE());
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
    guessIndexStream()
        .filter(i -> guess.get(i) == key.get(i)) // only keep exact matches
        .forEach(
            i -> {
              final char keyChar = key.get(i);
              reports.add(
                  new MatchReport(new IndexedCharacter(i, keyChar), MatchReportStatus.CharMatch));

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
    guessIndexStream()
        .filter(i -> !matchedGuesses[i]) // make sure we haven't matched this already
        .forEach(
            i -> {
              // For each char in the key, check if it matches our current guess char
              // and hasn't been matched yet
              IntStream.range(0, key.size())
                  .filter(j -> !matchedKeys[j]) // key position not already matched
                  .filter(j -> key.get(j) == guess.get(i)) // chars match
                  .findFirst()
                  .ifPresent(
                      j -> {
                        matchedKeys[j] = true;
                        matchedGuesses[i] = true;
                        reports.add(
                            new MatchReport(
                                new IndexedCharacter(i, guess.get(i)),
                                MatchReportStatus.CharElsewhere));
                      });
            });

    return this;
  }

  /**
   * Process all indeces where the guess totally failed to match with any characters in the key. All
   * that must be done here is report the character at i as absent.
   */
  private NGramMatcher matchAbsentCharacters() {
    guessIndexStream()
        .filter(i -> !matchedGuesses[i])
        .forEach(
            i ->
                reports.add(
                    new MatchReport(
                        new IndexedCharacter(i, guess.get(i)), MatchReportStatus.CharAbsent)));

    return this;
  }
}
