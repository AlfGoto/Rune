package com.alfgoto.rune.foundations.registry;

/** Thrown when a definition is registered with an id that is already taken. */
public class DuplicateIdException extends IllegalArgumentException {

    public DuplicateIdException(String registryName, String id) {
        super("Duplicate id '" + id + "' in registry '" + registryName + "'");
    }
}
