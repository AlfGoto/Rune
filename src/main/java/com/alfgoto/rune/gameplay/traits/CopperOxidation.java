package com.alfgoto.rune.gameplay.traits;

import com.alfgoto.rune.foundations.config.TraitConfig;
import com.alfgoto.rune.gameplay.effects.Scale;
import com.alfgoto.rune.gameplay.items.ItemRegistry;
import com.alfgoto.rune.gameplay.items.RuneItem;
import com.alfgoto.rune.text.Text;
import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

/**
 * The Copper Trait (docs/jobs/mining.md, woodcutting.md): a tool oxidizes as it breaks blocks, and gets faster.
 * A Copper tool is a Rune Tool with a Trait binding on the {@code copper_stage} Scale.
 *
 * <p>The blocks broken and the wax are kept on the stack's PDC ({@code rune:copper_blocks} and
 * {@code rune:waxed}), so they travel with the tool. The stage is 0 to 3 (Copper, Exposed, Weathered,
 * Oxidized): one step per {@code tools.copper.blocks-per-stage} blocks. Wax locks the stage for good.
 * The item model follows the stage and the wax (e.g. {@code waxed_exposed_copper_pickaxe}).
 */
public final class CopperOxidation {

    /** The id of the Scale: stage / 3. */
    public static final String SCALE_ID = "copper_stage";

    /** The last stage (Oxidized). */
    public static final int LAST_STAGE = 3;

    private static final String[] STAGE_PREFIX = {"", "exposed_", "weathered_", "oxidized_"};

    private final ItemRegistry items;
    private final TraitConfig config;
    private final NamespacedKey blocksKey;
    private final NamespacedKey waxedKey;

    public CopperOxidation(Plugin plugin, ItemRegistry items, TraitConfig config) {
        this.items = items;
        this.config = config;
        this.blocksKey = new NamespacedKey(plugin, "copper_blocks");
        this.waxedKey = new NamespacedKey(plugin, "waxed");
    }

    /** The Scale {@code copper_stage}: the stage of the tool divided by 3 (0 for any other stack). */
    public Scale scale() {
        return new Scale() {
            @Override
            public String id() {
                return SCALE_ID;
            }

            @Override
            public double at(@Nullable Player player, @Nullable ItemStack stack) {
                return stack == null ? 0 : stage(stack) / (double) LAST_STAGE;
            }
        };
    }

    /** Whether this item is a Copper tool: a Rune Tool whose Trait uses the {@code copper_stage} Scale. */
    public static boolean isCopper(RuneItem item) {
        return item.toolSpec()
                .map(spec -> spec.traitBindings().stream().anyMatch(b -> b.scale().id().equals(SCALE_ID)))
                .orElse(false);
    }

    /** The Copper tool this stack is, or empty. */
    public Optional<RuneItem> copperTool(@Nullable ItemStack stack) {
        return items.of(stack).filter(CopperOxidation::isCopper);
    }

    /** Blocks counted on the stack (0 if none). Bone-meal-grown blocks count less, so it is a decimal. */
    public double blocks(@Nullable ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return 0;
        }
        Double value = stack.getItemMeta().getPersistentDataContainer().get(blocksKey, PersistentDataType.DOUBLE);
        return value == null ? 0 : value;
    }

    /** The stage of the stack, 0 to 3. */
    public int stage(@Nullable ItemStack stack) {
        return stageAt(blocks(stack), config.copperBlocksPerStage());
    }

    static int stageAt(double blocks, int blocksPerStage) {
        return (int) Math.min(LAST_STAGE, Math.floor(blocks / blocksPerStage + 1e-9));
    }

    /** Whether the stage is locked by wax. */
    public boolean waxed(@Nullable ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().has(waxedKey, PersistentDataType.BYTE);
    }

    /** The item model for a stage: {@code copper_pickaxe}, {@code exposed_copper_pickaxe}, {@code waxed_…}. */
    public static String model(RuneItem item, int stage, boolean waxed) {
        return (waxed ? "waxed_" : "") + STAGE_PREFIX[stage] + item.model();
    }

    /**
     * Counts {@code amount} more blocks on a Copper tool. Does nothing when the stack is not a Copper tool, is
     * waxed or is already Oxidized. Returns whether it counted.
     */
    public boolean add(ItemStack stack, double amount) {
        RuneItem item = copperTool(stack).orElse(null);
        if (item == null || !(amount > 0) || waxed(stack) || stage(stack) >= LAST_STAGE) {
            return false;
        }
        double next = Math.min(blocks(stack) + amount, (double) LAST_STAGE * config.copperBlocksPerStage());
        set(stack, item, next, false);
        return true;
    }

    /** Sets the stage of a Copper tool (blocks = stage x blocks per stage). Returns false for any other stack. */
    public boolean setStage(ItemStack stack, int stage) {
        RuneItem item = copperTool(stack).orElse(null);
        if (item == null || stage < 0 || stage > LAST_STAGE) {
            return false;
        }
        set(stack, item, (double) stage * config.copperBlocksPerStage(), waxed(stack));
        return true;
    }

    /** Locks the stage of a Copper tool with wax. Returns false when it is not a Copper tool or is already waxed. */
    public boolean wax(ItemStack stack) {
        RuneItem item = copperTool(stack).orElse(null);
        if (item == null || waxed(stack)) {
            return false;
        }
        set(stack, item, blocks(stack), true);
        return true;
    }

    private void set(ItemStack stack, RuneItem item, double blocks, boolean waxed) {
        int stage = stageAt(blocks, config.copperBlocksPerStage());
        items.update(stack, meta -> {
            PersistentDataContainer data = meta.getPersistentDataContainer();
            data.set(blocksKey, PersistentDataType.DOUBLE, blocks);
            if (waxed) {
                data.set(waxedKey, PersistentDataType.BYTE, (byte) 1);
            }
            meta.setItemModel(new NamespacedKey(Text.NAMESPACE, model(item, stage, waxed)));
        });
    }
}
