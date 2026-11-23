package com.alfgoto.rune.gameplay.woodcutting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ReplantRulesTest {

    @Test
    void logsGiveTheirSapling() {
        assertEquals(Optional.of(Material.OAK_SAPLING), ReplantRules.saplingFor(Material.OAK_LOG));
        assertEquals(Optional.of(Material.DARK_OAK_SAPLING), ReplantRules.saplingFor(Material.STRIPPED_DARK_OAK_WOOD));
        assertEquals(Optional.of(Material.MANGROVE_PROPAGULE), ReplantRules.saplingFor(Material.MANGROVE_LOG));
    }

    @Test
    void stemsGiveTheirFungus() {
        assertEquals(Optional.of(Material.CRIMSON_FUNGUS), ReplantRules.saplingFor(Material.CRIMSON_STEM));
        assertEquals(Optional.of(Material.WARPED_FUNGUS), ReplantRules.saplingFor(Material.STRIPPED_WARPED_HYPHAE));
    }

    @Test
    void otherBlocksGiveNothing() {
        assertTrue(ReplantRules.saplingFor(Material.STONE).isEmpty());
    }
}
