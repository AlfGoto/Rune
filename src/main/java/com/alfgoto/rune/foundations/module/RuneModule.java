package com.alfgoto.rune.foundations.module;

import java.util.Set;

/**
 * One piece of the plugin (Triggers, Block Tracking, Jobs, Mining content…). See ADR 0004.
 *
 * <p>A module registers itself: tag the class with {@code @AutoService(RuneModule.class)} and give
 * it a public no-argument constructor. {@code RunePlugin} finds it at startup, so adding a module
 * never touches {@code RunePlugin}.
 */
public interface RuneModule {

    /** Registers listeners, subcommands and services. Called once, in dependency order. */
    void enable(RuneContext ctx);

    /** Called on shutdown, in reverse order. */
    default void disable() {}

    /** Modules that must be enabled before this one (usually because it uses their services). */
    default Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of();
    }
}
