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

  @Override
  public Iterator<NGram> iterator() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'iterator'");
  }

  public final class Iterator implements java.util.Iterator<NGram> {
    private int cursor
  };
}
