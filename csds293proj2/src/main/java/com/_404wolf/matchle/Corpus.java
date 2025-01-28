package com._404wolf.matchle;

import java.util.Iterator;
import java.util.Set;

public final class Corpus implements Iterable<NGram> {
  private final Set<NGram> corpus;

  private Corpus(Set<NGram> corpus) {
    this.corpus = corpus;
  }

  public Set<NGram> getCorpus() {
    return corpus;
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
  public Iterator<NGram> iterator() {
    return corpus.iterator();
  }
}
