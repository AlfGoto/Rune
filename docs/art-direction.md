# Art direction — item textures & menus (2026-11-15)

Validated on texture batch v2: pickaxes, axes, components, menu icons, first two menus.

## References
- **Vanilla Java 26.x item art** is the base. Every pickaxe and axe uses the exact vanilla 26.x template (checked pixel-for-pixel against `iron_pickaxe.png` / `iron_axe.png`); only palettes and a few pixels change. Same 45° diagonal, same 1px dark outline, 3–4 shade ramps, vanilla stick colours.
- **Vanilla palettes** for every material: flint, coal, copper (+ exposed / weathered / oxidized), iron, gold, diamond, netherite, obsidian / crying obsidian, amethyst, prismarine, echo shard / sculk, emerald, redstone, bone, raw iron, magma, blaze / breeze rod, each wood's planks + log, etc. Source used: 26.1 textures from the `minecraft-assets` npm package.
- **Tinkers' Construct** for how tools are built: visible parts — head, binding knot (where head meets handle), handle, grip, pommel. Theme shows in the head's material and colour, not in extra detail.
- **Vanilla GUI** (`generic_54`) for menus: same frame, bevels and slot style, with small Rune accents (rune glyphs, gold slot brackets).

## Rules
- Strict 16×16, no anti-aliasing, no glow, no rarity borders (the name colour shows Rarity).
- Upgrade lines look related: each step is the previous tool, upgraded (bands, tips or cores carry over).
- Copper has 4 oxidation stages; waxed copies add a small honeycomb gloss (vanilla waxed items look identical, so we add a tell).
- Tools use `minecraft:item/handheld`; other items `minecraft:item/generated`.
- File names: snake_case English; tools with their own name have no suffix (`tunneler.png`, `briseroc.png`, `worldpick.png`, `starbreaker.png`, `worldheart.png`, `yggdrasil.png`, `timberfall.png`…).

## Fanciness ladder (same for every Job)
| Rarity | What it adds |
|---|---|
| Common | vanilla shape, a few material pixels, string/stone binding knot |
| Uncommon | + Reinforced Handle look (leather grip, iron end cap), metal knot |
| Rare | + gold Gem Setting knot with a gem, sharpened tips, themed handle (blaze rod, logs…); wood axes get a diamond-honed edge |
| Epic | + special head shape (heavy / wide pickaxe head, bigger axe blade), two materials, dark handle + pommel |
| Legendary | + gold finial above the head, gold filigree, gem pommel; double-bit axes |
| Relic / Fused | unique silhouette built around its motif (Warden antennae, Starbreaker star, evoker-fang blade…) |

## Menus (start)
- Each menu background is a 176×222 PNG (same frame as `generic_54`), shown through a bitmap font glyph in the menu title shifted with negative space. Clickable things stay real chest slots, so `Menu` / `Button` don't change.
- Rune Codex main menu: player head (Account level) on top, three 3×3 panels (Jobs = wood, Quests = parchment, Runes = amethyst); the gold-framed middle slot holds the icon, the 8 slots around it are invisible buttons doing the same. Back on `Menu.backSlot()`.
- Rune loadout: tool on display, 5 Rune slots in a deepslate band (first 2 = Active Slots, gold brackets), capacity bar made of pip items (used / free / over) over a groove.

## Runes (layered, 2026-11-22)
A Rune texture is never drawn whole: it is one `minecraft:item/generated` model with stacked layers, all 16×16 on the same canvas. Generator: `tools/texture-gen/v3/runes.py`; contact sheets on the Design canvas "Rune textures — Runes (layered)" (https://claude.ai/artifact/A7xpaMRtAFm31sr1MMZNzH).

| Layer | File | One per | Rule |
|---|---|---|---|
| 0 stone | `item/rune/base/<rarity>.png` | Rarity | Common stone · Uncommon stone + copper corners · Rare deepslate + gold corners + diamond setting · Epic obsidian + crying obsidian rim + netherite corners · Legendary netherite + gold frame and finial, **animated sparkles** (12 frames × 2 ticks, `.mcmeta`) |
| 1 logo | `item/rune/logo/<rune>.png` | Rune | 8×8 symbol in the centre (x4–11, y3–10) + 1px engraving shadow below-right |
| 2 socket | `item/rune/socket/<rune>_<variant>.png` | variant | only Specialist, Affinity, Sanctuary: the logo has an empty socket, this layer paints the material in |
| last numeral | `item/rune/rank/<n>[_max].png` | Rank | Roman numeral on the bottom strip, white with a dark edge; gold (`_max`) when Rank = Max Rank |

**How a Rune gets its look**
- **Stone = the Rune's Rarity**, never anything else. Same material ladder as the tools and armor.
- **Logo = what the Rune does**, one symbol per effect, readable at 16 px. Colour = the material of the thing it acts on (Haste yellow, Fortune emerald, Smelt flame, Veinminer diamond, wood Runes leaf green + log brown). Two colours max (primary + secondary ramp from `palettes.py`).
- **Variants share the logo.** `Specialist: <ore / wood>`, `Affinity: <place>`, `Sanctuary: <biome>` differ only by the socket colour = that ore / wood / place's vanilla palette. A new variant is one line in `VARIANTS`.
- **Rank = numeral.** 3-pixel font (I, V, X). I → XX fit (XVIII is the widest); higher Ranks show XX. Max Rank I Runes (Silk Touch, Replant) show no numeral. Runes with no Max Rank (Deepstride, Canopy Stride) go I → XX, never gold.
- The logo area and the numeral strip stay clean on every stone: decoration (corners, frame, sparkles) stays on the rim or outside the stone.

**In the pack**: item definition `rune:<rune id>` (`haste`, `specialist_diamond`, `affinity_lush_caves`…) is a `range_dispatch` on `custom_model_data` `floats[0]` = the Rank; it picks model `rune:item/rune/<id>_<rank>`. Missing value = Rank I. The Max Rank (gold numeral) is baked from `RUNES` in `runes.py` (= the Job docs): if a Max Rank changes in config, change it there and re-export. The plugin has to set the item model to the Rune id and write the Rank into `custom_model_data` (not done yet: Runes still use `test_rune`).

| Rune | Rarity | Max Rank | Logo |
|---|---|---|---|
| Haste | Common | V | lightning bolt |
| Sturdy | Common | IV | iron shield |
| Specialist: <ore / wood> | Common | III | ring with a gem socket |
| Fortune | Uncommon | III | emerald four-leaf clover |
| Magnet | Uncommon | III | red horseshoe magnet, iron tips |
| Ore Sense | Uncommon | III | cyan eye |
| Affinity: <place> | Uncommon | III | map pin with a socket |
| Deepstride | Uncommon | — | cyan arrow pointing down |
| Leaf Fall | Uncommon | III | falling leaf |
| Orchard | Uncommon | III | apple |
| Canopy Stride | Uncommon | — | leather boot with a leaf |
| Smelt | Rare | III | flame |
| Cache Hunter | Rare | III | gold key |
| Ore Dash | Rare | III | breeze arrow with speed lines |
| Replant | Rare | I | sapling |
| Tree Dash | Rare | III | tree with speed lines |
| Sanctuary: <biome> | Rare | III | pink heart with a socket |
| Blast Mine | Epic | III | red burst |
| Ore Bloom | Epic | V | pink flower on a stem |
| Silk Touch | Epic | I | spool of purple thread |
| Treefeller | Epic | III | falling tree |
| Boomerang | Epic | III | wooden boomerang |
| Great Blast | Legendary | III | big flame burst, white core |
| Veinminer | Legendary | III | diamond vein |
| Great Fell | Legendary | III | three trees |

## Rune Cache and Rune Codex (2026-11-22)
- **Rune Cache** `rune_cache_<rarity>`: a sealed coffer (`caches.py`). Body wood + metal bands + a seal in the **Rarity's name colour** (white, green, aqua, magenta, gold): Common oak + cobblestone · Uncommon spruce + copper · Rare dark oak + gold · Epic obsidian + crying obsidian · Legendary netherite + gold, same animated sparkles as the Legendary stone.
- **Rune Codex** `rune_codex`: the vanilla 26.1 book, indigo leather, gold corners, a glowing cyan rune on the cover.

## Tooling
- Generator: `tools/texture-gen/` (Python + Pillow, see its README). A colour change is a one-line edit; `export_all.py` rebuilds every PNG and pack file.
- Open points: real poplar palette (no poplar texture in 26.1, colours invented); some Relic loose pixels (hanging moss / roots) to check in game.
- Next batches: Blueprint, font glyphs (Rarity + Job icons), menus v3 (Jobs, Quests, Cache reel).
