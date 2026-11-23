package com.alfgoto.rune.gameplay.detection;

/** Numbers of the Echo ping, read from config (runes.echo_ping). */
public record DetectionSettings(int echoRadius, int echoTicks, int echoIntervalTicks) {

    public static final int MAX_RADIUS = 16;

    public DetectionSettings {
        echoRadius = Math.max(1, Math.min(MAX_RADIUS, echoRadius));
        echoTicks = Math.max(1, echoTicks);
        echoIntervalTicks = Math.max(20, echoIntervalTicks);
    }
}
