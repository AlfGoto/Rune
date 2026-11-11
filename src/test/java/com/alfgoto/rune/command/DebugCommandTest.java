package com.alfgoto.rune.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

class DebugCommandTest {

    static final class Tool implements Subcommand {
        final String name;
        final List<List<String>> calls = new ArrayList<>();

        Tool(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public void execute(CommandSender sender, List<String> args) {
            calls.add(args);
        }
    }

    @Test
    void routesToTheTool() {
        DebugCommand debug = new DebugCommand();
        Tool blocks = new Tool("blocks");
        debug.register(blocks);
        debug.register(new Tool("events"));
        debug.execute(null, List.of("blocks", "on"));
        assertEquals(List.of(List.of("on")), blocks.calls);
    }

    @Test
    void completesToolNames() {
        DebugCommand debug = new DebugCommand();
        debug.register(new Tool("blocks"));
        debug.register(new Tool("events"));
        assertEquals(List.of("blocks"), debug.complete(null, List.of("b")));
    }

    @Test
    void sameToolTwiceFails() {
        DebugCommand debug = new DebugCommand();
        debug.register(new Tool("blocks"));
        assertThrows(IllegalArgumentException.class, () -> debug.register(new Tool("blocks")));
    }
}
