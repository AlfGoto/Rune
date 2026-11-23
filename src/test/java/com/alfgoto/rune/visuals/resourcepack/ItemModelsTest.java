package com.alfgoto.rune.visuals.resourcepack;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alfgoto.rune.RunePlugin;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

/** Every real item shows a real texture: its model has an item definition in the pack, and only test items use the test model. */
class ItemModelsTest {

    private ItemRegistry items;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        RunePlugin plugin = MockBukkit.load(RunePlugin.class);
        items = plugin.context().get(ItemRegistry.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void everyItemModelExistsInThePack() {
        List<String> missing = items.all().stream()
                .map(RuneItem::model)
                .filter(model -> getClass().getResource("/pack/assets/rune/items/" + model + ".json") == null)
                .distinct()
                .toList();
        assertTrue(missing.isEmpty(), "No item definition in the pack for: " + missing);
    }

    @Test
    void onlyTestItemsUseTheTestModel() {
        List<String> wrong = items.all().stream()
                .filter(item -> item.model().equals("test_rune") && !item.id().startsWith("test_"))
                .map(RuneItem::id)
                .toList();
        assertTrue(wrong.isEmpty(), "Real items still on the test model (draw them in tools/texture-gen): " + wrong);
    }
}
