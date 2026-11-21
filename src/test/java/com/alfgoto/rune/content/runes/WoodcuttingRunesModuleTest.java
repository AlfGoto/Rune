package com.alfgoto.rune.content.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class WoodcuttingRunesModuleTest {

    @Test
    void hasTheTwelveWoodTypes() {
        assertEquals(12, WoodcuttingRunesModule.WOODS.size());
        WoodcuttingRunesModule.WOODS.forEach((id, blocks) -> assertTrue(!blocks.isEmpty(), id + " has no blocks"));
    }

    @Test
    void woodFamilyHasLogsAndStrippedVersions() {
        var oak = WoodcuttingRunesModule.WOODS.get("oak");
        assertTrue(oak.contains(Material.OAK_LOG));
        assertTrue(oak.contains(Material.STRIPPED_OAK_LOG));
        assertTrue(oak.contains(Material.OAK_WOOD));
        assertTrue(WoodcuttingRunesModule.WOODS.get("crimson").contains(Material.CRIMSON_STEM));
    }
}
