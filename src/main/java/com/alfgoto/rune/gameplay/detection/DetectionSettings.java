package com.alfgoto.rune.gameplay.detection;

/** Numbers of the detection effects, read from config (runes.ore_sense and runes.echo_ping). */
public record DetectionSettings(
        int oreSenseRadius, int oreSenseTicks, int echoRadius, int echoTicks, int echoIntervalTicks) {

    public static final int MAX_RADIUS = 32;

    public DetectionSettings {
        oreSenseRadius = Math.max(1, Math.min(MAX_RADIUS, oreSenseRadius));
        echoRadius = Math.max(1, Math.min(MAX_RADIUS, echoRadius));
        oreSenseTicks = Math.max(1, oreSenseTicks);
        echoTicks = Math.max(1, echoTicks);
        echoIntervalTicks = Math.max(20, echoIntervalTicks);
    }
}
