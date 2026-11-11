package com.alfgoto.rune.foundations.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ModuleLoaderTest {

    abstract static class Fake implements RuneModule {
        @Override
        public void enable(RuneContext ctx) {}
    }

    static final class Storage extends Fake {}

    static final class Triggers extends Fake {}

    static final class Jobs extends Fake {
        @Override
        public Set<Class<? extends RuneModule>> dependsOn() {
            return Set.of(Triggers.class, Storage.class);
        }
    }

    static final class Mining extends Fake {
        @Override
        public Set<Class<? extends RuneModule>> dependsOn() {
            return Set.of(Jobs.class);
        }
    }

    static final class A extends Fake {
        @Override
        public Set<Class<? extends RuneModule>> dependsOn() {
            return Set.of(B.class);
        }
    }

    static final class B extends Fake {
        @Override
        public Set<Class<? extends RuneModule>> dependsOn() {
            return Set.of(A.class);
        }
    }

    private static List<Class<?>> classes(List<RuneModule> modules) {
        return modules.stream().<Class<?>>map(Object::getClass).toList();
    }

    @Test
    void dependenciesComeFirst() {
        var order = classes(ModuleLoader.sort(List.of(new Mining(), new Jobs(), new Triggers(), new Storage())));
        assertTrue(order.indexOf(Triggers.class) < order.indexOf(Jobs.class));
        assertTrue(order.indexOf(Storage.class) < order.indexOf(Jobs.class));
        assertTrue(order.indexOf(Jobs.class) < order.indexOf(Mining.class));
    }

    @Test
    void orderDoesNotDependOnInputOrder() {
        var a = classes(ModuleLoader.sort(List.of(new Mining(), new Jobs(), new Triggers(), new Storage())));
        var b = classes(ModuleLoader.sort(List.of(new Storage(), new Triggers(), new Jobs(), new Mining())));
        assertEquals(a, b);
    }

    @Test
    void missingDependencyFailsClearly() {
        var e = assertThrows(ModuleException.class, () -> ModuleLoader.sort(List.of(new Mining())));
        assertTrue(e.getMessage().contains("Jobs"), e.getMessage());
    }

    @Test
    void cycleFailsClearly() {
        var e = assertThrows(ModuleException.class, () -> ModuleLoader.sort(List.of(new A(), new B())));
        assertTrue(e.getMessage().contains("cycle"), e.getMessage());
    }
}
