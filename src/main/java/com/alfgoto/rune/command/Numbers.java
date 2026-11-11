package com.alfgoto.rune.command;

import java.util.Locale;

/** Number parsing and display for admin commands. */
final class Numbers {

    /** Largest level a command accepts: keeps the level loop short. */
    static final int MAX_LEVEL = 10_000;

    private Numbers() {}

    /** An XP amount of 0 or more, or null if the text is not one. */
    static Double parseXp(String text) {
        try {
            double value = Double.parseDouble(text);
            return Double.isFinite(value) && value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** A level from 1 to {@link #MAX_LEVEL}, or null if the text is not one. */
    static Integer parseLevel(String text) {
        try {
            int value = Integer.parseInt(text);
            return value >= 1 && value <= MAX_LEVEL ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** One decimal at most, e.g. {@code 12.5} or {@code 40}. */
    static String format(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }
}
