package com.alfgoto.rune.gameplay.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.alfgoto.rune.foundations.config.ArmorConfig;
import org.junit.jupiter.api.Test;

class DepthScaleTest {

    private final DepthScale scale = DepthScale.of(ArmorConfig.defaults());

    @Test
    void isZeroAtTheTopAndFullAtTheBottom() {
        assertEquals(0.0, scale.fraction(64), 1e-9);
        assertEquals(1.0, scale.fraction(-64), 1e-9);
    }

    @Test
    void growsLinearlyWithDepth() {
        assertEquals(0.5, scale.fraction(0), 1e-9);
        assertEquals(0.25, scale.fraction(32), 1e-9);
        assertEquals(0.75, scale.fraction(-32), 1e-9);
    }

    @Test
    void staysClampedOutsideTheScale() {
        assertEquals(0.0, scale.fraction(200), 1e-9);
        assertEquals(1.0, scale.fraction(-200), 1e-9);
    }

    @Test
    void aCustomScaleIsUsed() {
        DepthScale custom = new DepthScale(0, -32);
        assertEquals(0.5, custom.fraction(-16), 1e-9);
    }

    @Test
    void topMustBeAboveBottom() {
        assertThrows(IllegalArgumentException.class, () -> new DepthScale(-64, 64));
        assertThrows(IllegalArgumentException.class, () -> new DepthScale(10, 10));
    }
}
