package com.alfgoto.rune.foundations.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/** Finds every {@link RuneModule} and orders them so each comes after what it depends on. */
public final class ModuleLoader {

    private ModuleLoader() {}

    /** Every module tagged with {@code @AutoService(RuneModule.class)}, in enable order. */
    public static List<RuneModule> load(ClassLoader classLoader) {
        List<RuneModule> found = new ArrayList<>();
        ServiceLoader.load(RuneModule.class, classLoader).forEach(found::add);
        return sort(found);
    }

    /**
     * Orders modules by {@link RuneModule#dependsOn()}. Ties are broken by class name, so the order
     * is the same on every start. Throws {@link ModuleException} on a missing dependency or a cycle.
     */
    public static List<RuneModule> sort(Collection<? extends RuneModule> modules) {
        Map<Class<?>, RuneModule> byClass = new LinkedHashMap<>();
        modules.stream()
                .sorted(Comparator.comparing(m -> m.getClass().getName()))
                .forEach(m -> {
                    if (byClass.put(m.getClass(), m) != null) {
                        throw new ModuleException(m.getClass().getSimpleName() + " is loaded twice");
                    }
                });
        for (RuneModule module : byClass.values()) {
            for (Class<?> dependency : module.dependsOn()) {
                if (!byClass.containsKey(dependency)) {
                    throw new ModuleException(module.getClass().getSimpleName() + " depends on "
                            + dependency.getSimpleName() + ", which is not loaded (missing @AutoService?)");
                }
            }
        }
        List<RuneModule> ordered = new ArrayList<>();
        Map<Class<?>, Boolean> done = new HashMap<>(); // false = visiting, true = done
        for (RuneModule module : byClass.values()) {
            visit(module, byClass, done, ordered, new ArrayList<>());
        }
        return ordered;
    }

    private static void visit(
            RuneModule module,
            Map<Class<?>, RuneModule> byClass,
            Map<Class<?>, Boolean> done,
            List<RuneModule> ordered,
            List<String> path) {
        Class<?> type = module.getClass();
        path.add(type.getSimpleName());
        Boolean state = done.get(type);
        if (Boolean.TRUE.equals(state)) {
            path.removeLast();
            return;
        }
        if (Boolean.FALSE.equals(state)) {
            throw new ModuleException("Module dependency cycle: " + String.join(" -> ", path));
        }
        done.put(type, false);
        module.dependsOn().stream()
                .sorted(Comparator.comparing(Class::getName))
                .forEach(dependency -> visit(byClass.get(dependency), byClass, done, ordered, path));
        done.put(type, true);
        ordered.add(module);
        path.removeLast();
    }
}
