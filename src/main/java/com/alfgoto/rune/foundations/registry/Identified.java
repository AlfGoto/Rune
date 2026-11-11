package com.alfgoto.rune.foundations.registry;

/** Anything stored in a {@link Registry}: a definition with a unique id. */
public interface Identified {

    /** The unique id of this definition, e.g. "mining". */
    String id();
}
