package com.alfgoto.rune.foundations.registry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class RegistryTest {

    private record Def(String id) implements Identified {}

    @Test
    void registersAndLooksUpById() {
        Registry<Def> registry = new Registry<>("jobs");
        Def mining = registry.register(new Def("mining"));
        assertSame(mining, registry.get("mining"));
        assertTrue(registry.find("mining").isPresent());
        assertTrue(registry.contains("mining"));
        assertEquals(1, registry.size());
    }

    @Test
    void unknownIdIsEmptyOrThrows() {
        Registry<Def> registry = new Registry<>("jobs");
        assertTrue(registry.find("nope").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> registry.get("nope"));
    }

    @Test
    void duplicateIdIsRejected() {
        Registry<Def> registry = new Registry<>("jobs");
        registry.register(new Def("mining"));
        assertThrows(DuplicateIdException.class, () -> registry.register(new Def("mining")));
        assertEquals(1, registry.size());
    }

    @Test
    void blankIdIsRejected() {
        Registry<Def> registry = new Registry<>("jobs");
        assertThrows(IllegalArgumentException.class, () -> registry.register(new Def(" ")));
    }

    @Test
    void allKeepsRegistrationOrderAndIsReadOnly() {
        Registry<Def> registry = new Registry<>("jobs");
        registry.register(new Def("b"));
        registry.register(new Def("a"));
        assertEquals(List.of("b", "a"), registry.all().stream().map(Def::id).toList());
        assertThrows(UnsupportedOperationException.class, () -> registry.all().clear());
    }
}
