package com._404wolf.matchle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Represents a character with its index in the NGram. */
record IndexedCharacter(int index, Character character) {}

/** Represents an n-gram, which is a sequence of n characters. This class is immutable. */
public final class NGram implements Iterable<IndexedCharacter> {
  private final List<Character> ngram;
  private final Set<Character> charset;

  /**
   * Private constructor to initialize the NGram. Use the static factory methods to create instances
   * of NGram.
   */
  private NGram(ArrayList<Character> ngram) {
    this.ngram = Collections.unmodifiableList(ngram);
    this.charset = Collections.unmodifiableSet(new HashSet<Character>(ngram));
  }

  /** Exception thrown when a null character is encountered in NGram creation. */
  public static final class NullCharacterException extends Exception {
    private static final long serialVersionUID = 1234567890L;
    private final int index;

    /**
     * Constructs a new NullCharacterException with the specified index.
     *
     * @param index the position of the null character
     */
    public NullCharacterException(int index) {
      super("Null character found at index: " + index);
      this.index = index;
    }

    /**
     * Gets the index of the null character.
     *
     * @return the index where the null character was found
     */
    public int getIndex() {
      return index;
    }

    /**
     * Validates a list of characters, ensuring none are null.
     *
     * @param ngram the list of characters to validate
     * @return the validated list of characters
     * @throws NullPointerException if the argument is null
     * @throws IllegalArgumentException if any character in the argument is null
     */
    public static final List<Character> validate(List<Character> ngram) {
      Objects.requireNonNull(ngram, "NGram list cannot be null");

      IntStream.range(0, ngram.size())
          .filter(i -> ngram.get(i) == null)
          .findFirst()
          .ifPresent(
              i -> {
                throw new IllegalArgumentException(new NullCharacterException(i));
              });

      return ngram;
    }
  }

  /**
   * Creates a new NGram from a List of Characters.
   *
   * @param characters the List of Characters to create the NGram from
   * @return a new NGram instance
   * @throws NullPointerException if the argument is null
   * @throws IllegalArgumentException if any character in the argument is null
   */
  public static final NGram from(List<Character> characters) throws IllegalArgumentException {
    Objects.requireNonNull(characters, "Character list cannot be null");

    return new NGram(new ArrayList<>(NullCharacterException.validate(characters)));
  }

  /**
   * Creates a new NGram from a String.
   *
   * @param word the String to create the NGram from
   * @return a new NGram instance
   * @throws NullPointerException if the argument is null
   */
  public static final NGram from(String word) throws NullPointerException {
    Objects.requireNonNull(word, "Word cannot be null");

    List<Character> charList = word.chars().mapToObj(ch -> (char) ch).collect(Collectors.toList());
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
    return ngram.get(Objects.checkIndex(index, ngram.size()));
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
   * Checks if the given IndexedCharacter matches the character at its index in this NGram.
   *
   * @param c the IndexedCharacter to check
   * @return true if the character matches at the given index, false otherwise
   */
  public boolean matches(IndexedCharacter c) {
    try {
      return ngram.get(Objects.checkIndex(c.index(), ngram.size())).equals(c.character());
    } catch (IndexOutOfBoundsException e) {
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
   * Checks if the character of the given IndexedCharacter is present in this NGram at a different
   * index.
   *
   * @param c the IndexedCharacter to check
   * @return true if the character is present at a different index, false otherwise
   */
  public boolean containsElsewhere(IndexedCharacter c) {
    return IntStream.range(0, ngram.size())
        .filter(i -> i != c.index()) // get all the "wrong" elements
        .mapToObj(i -> ngram.get(i)) // map them to ngrams
        .anyMatch(ch -> ch.equals(c.character())); // make sure that they aren't ours
  }

  /**
   * Returns a Stream of IndexedCharacters representing this NGram.
   *
   * @return a Stream of IndexedCharacters
   */
  public Stream<IndexedCharacter> stream() {
    return IntStream.range(0, ngram.size())
        .limit(ngram.size())
        .mapToObj(i -> new IndexedCharacter(i, ngram.get(i)));
  }

  @Override
  public java.util.Iterator<IndexedCharacter> iterator() {
    return IntStream.range(0, ngram.size())
        .mapToObj(i -> new IndexedCharacter(i, ngram.get(i)))
        .iterator();
  }

  public final class Iterator implements java.util.Iterator<IndexedCharacter> {
    private final java.util.Iterator<IndexedCharacter> delegate = iterator();

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public IndexedCharacter next() {
      return delegate.next();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    NGram other = (NGram) obj;
    return Objects.equals(ngram, other.ngram);
  }

  @Override
  public int hashCode() {
    return ngram.hashCode();
  }

  @Override
  public String toString() {
    return ngram.stream().map(String::valueOf).collect(Collectors.joining());
  }
}
