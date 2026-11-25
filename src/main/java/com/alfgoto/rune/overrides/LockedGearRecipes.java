package com.alfgoto.rune.overrides;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.inventory.Recipe;

/** Removes the vanilla recipes whose result is Locked Gear, so the recipe book stops showing them. */
public final class LockedGearRecipes {

    private LockedGearRecipes() {}

    /** Removes every Locked Gear recipe from the server and undiscovers it for online players. Returns the keys. */
    public static List<NamespacedKey> removeAll(Server server) {
        List<NamespacedKey> keys = new ArrayList<>();
        Iterator<Recipe> it = server.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (recipe instanceof Keyed keyed && LockedGear.isLocked(recipe.getResult())) {
                keys.add(keyed.getKey());
            }
        }
        keys.forEach(server::removeRecipe);
        server.getOnlinePlayers().forEach(player -> player.undiscoverRecipes(keys));
        return keys;
    }
}
