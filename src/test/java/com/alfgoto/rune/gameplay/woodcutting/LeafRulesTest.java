package com.alfgoto.rune.gameplay.woodcutting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.gameplay.woodcutting.LeafRules.Offset;
import java.util.List;
import org.junit.jupiter.api.Test;

class LeafRulesTest {

    @Test
    void radiusTwoIsASphereWithoutTheLog() {
        List<Offset> offsets = LeafRules.offsets(2);
        assertFalse(offsets.contains(new Offset(0, 0, 0)));
        assertTrue(offsets.contains(new Offset(2, 0, 0)));
        assertTrue(offsets.contains(new Offset(1, 1, 1)));
        assertFalse(offsets.contains(new Offset(2, 2, 0)));
        assertFalse(offsets.contains(new Offset(3, 0, 0)));
    }

    @Test
    void biggerRadiusReachesFurther() {
        assertTrue(LeafRules.offsets(6).contains(new Offset(0, 6, 0)));
        assertEquals(0, LeafRules.offsets(0).size());
    }
}
