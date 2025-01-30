package com._404wolf.matchle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents an n-gram, which is a sequence of n characters. This class is
 * immutable.
 */
public final class NGram implements Iterable<NGram.IndexedCharacter> {
  private final ArrayList<Character> ngram;
  private final Set<Character> charset;

  /**
   * Private constructor to initialize the NGram.
   * Use the static factory methods to create instances of NGram.
   */
  private NGram(ArrayList<Character> ngram, Set<Character> charset) {
    this.ngram = ngram;
    this.charset = charset;
  }

  /**
   * Represents a character with its index in the NGram.
   */
  public record IndexedCharacter(int index, Character character) {
  }

  /**
   * Exception thrown when a null character is encountered in NGram creation.
   */
  public static final class NullCharacterException extends Exception {
    /**
     * Validates a list of characters, ensuring none are null.
     * 
     * @param ngram the list of characters to validate
     * @return the validated list of characters
     * @throws NullPointerException     if the argument is null
     * @throws IllegalArgumentException if any character in the argument is null
     */
    public static final List<Character> validate(List<Character> ngram) throws IllegalArgumentException {
      boolean valid = ngram.stream()
          .allMatch(c -> c != null);

      if (valid)
        return ngram;
      else
        throw new IllegalArgumentException("Character cannot be null");
    }
  }

  /**
   * Creates a new NGram from a List of Characters.
   * 
   * @param characters the List of Characters to create the NGram from
   * @return a new NGram instance
   * @throws NullPointerException     if the argument is null
   * @throws IllegalArgumentException if any character in the argument is null
   */
  public static final NGram from(List<Character> characters) throws NullPointerException, IllegalArgumentException {
    Objects.requireNonNull(characters);

    if (characters == null)
      throw new NullPointerException("Character list cannot be null");
    if (characters.contains(null))
      throw new IllegalArgumentException("Character cannot be null");

    return new NGram(
        new ArrayList<>(NullCharacterException.validate(characters)),
        Set.copyOf(characters));
  }

  /**
   * Creates a new NGram from a String.
   * 
   * @param word the String to create the NGram from
   * @return a new NGram instance
   * @throws NullPointerException if the argument is null
   */
  public static final NGram from(String word) throws NullPointerException {
    if (word == null)
      throw new NullPointerException("Word cannot be null");

    List<Character> charList = word.chars()
        .mapToObj(ch -> (char) ch)
        .collect(Collectors.toList());
    return NGram.from(charList);
  }

  /**
   * Returns the Character at the specified index in this NGram.
   * 
   * @param index the index of the Character to return
   * @return the Character at the specified index
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public Character get(int index) throws IndexOutOfBoundsException {
    if (index < 0 || index >= ngram.size()) {
      throw new IndexOutOfBoundsException("Index out of range: " + index);
    }
    return ngram.get(index);
  }

  /**
   * Returns the number of characters in this NGram.
   * 
   * @return the number of characters in this NGram
   */
  public int size() {
    return ngram.size();
  }

  /**
   * Checks if the given IndexedCharacter matches the character at its index in
   * this NGram.
   * 
   * @param c the IndexedCharacter to check
   * @return true if the character matches at the given index, false otherwise
   */
  public boolean matches(IndexedCharacter c) {
    if (0 <= c.index() && c.index() < ngram.size()) {
      return ngram.get(c.index()).equals(c.character());
    } else {
      return false;
    }
  }

  /**
   * Checks if the given character is present anywhere in this NGram.
   * 
   * @param c the character to check
   * @return true if the character is present, false otherwise
   */
  public boolean contains(char c) {
    return charset.contains(c);
  }

  /**
   * Checks if the character of the given IndexedCharacter is present in this
   * NGram at a different index.
   * 
   * @param c the IndexedCharacter to check
   * @return true if the character is present at a different index, false
   *         otherwise
   */
  public boolean containsElsewhere(IndexedCharacter c) {
    for (int i = 0; i < ngram.size(); i++) {
      if (i != c.index() && ngram.get(i).equals(c.character())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a Stream of IndexedCharacters representing this NGram.
   * 
   * @return a Stream of IndexedCharacters
   */
  public Stream<IndexedCharacter> stream() {
    return Stream.iterate(0, i -> i + 1)
        .limit(ngram.size())
        .map(i -> new IndexedCharacter(i, ngram.get(i)));
  }

  @Override
  public Iterator<IndexedCharacter> iterator() {
    return IntStream
        .range(0, ngram.size())
        .mapToObj(i -> new IndexedCharacter(i, ngram.get(i)))
        .iterator();
  }
  // TODO add Iterator subclass anyway (sub fine)

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    NGram other = (NGram) obj;
    return ngram.equals(other.ngram);
  }

  @Override
  public int hashCode() {
    return ngram.hashCode();
  }
}
