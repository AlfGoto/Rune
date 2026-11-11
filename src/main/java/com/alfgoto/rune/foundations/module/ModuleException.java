package com.alfgoto.rune.foundations.module;

/** The modules cannot be ordered: a dependency is missing, or two modules depend on each other. */
public final class ModuleException extends RuntimeException {

    public ModuleException(String message) {
        super(message);
    }
}
