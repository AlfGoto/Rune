package com.alfgoto.rune.gameplay.runes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alfgoto.rune.foundations.config.RuneConfig;
import org.junit.jupiter.api.Test;

class PlayerCapacityTest {

    private final PlayerCapacity capacity = new PlayerCapacity(RuneConfig.defaults());

    @Test
    void startsAtFiveAndGrowsByOnePerAccountLevel() {
        assertEquals(5, capacity.forAccountLevel(0));
        assertEquals(6, capacity.forAccountLevel(1));
        assertEquals(20, capacity.forAccountLevel(15));
    }

    @Test
    void isCappedAtForty() {
        assertEquals(40, capacity.forAccountLevel(35));
        assertEquals(40, capacity.forAccountLevel(1000));
    }
}
