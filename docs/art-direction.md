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

## Menus (v3, 2026-11-23)
- Each menu background is a PNG 176 × (114 + 18 × rows) px, the vanilla chest window (`generic_54`, stitched for the row count) with Rune panels painted over the menu area. It is a glyph of the `rune:menus` font (bitmap, `ascent` 13, `height` = image height) put in the menu title: a -8 px space glyph first (the title starts at x 8), then the background, then a -169 px space glyph so the title text is back in its usual place (`MenuBackground.title`). The glyph is white, or the dark title colour tints it.
- **Every slot that can hold an item is cut out (transparent)**: the menu's used slots and the 36 player slots. The vanilla slot and the item always show, whatever the draw order. Unused menu slots are painted over. A used slot can get a 1px ring on its own edge (never outside it, so two framed slots side by side never overlap): gold = the main thing, copper = Reroll Tokens, deepslate = Back, amethyst = Player Rune slots, oak log = page arrows.
- Generator `tools/texture-gen/v3/menus.py`; files `textures/gui/menu/<name>.png`; glyphs `\ue100`… in `font/menus.json`. A new menu = a function in `MENUS`, a value in `MenuBackground`, a re-export.

| Menu | Rows | Look |
|---|---|---|
| Rune Codex (`codex_main`) | 3 | three 3×3 boards: Jobs (oak planks), Quests (parchment), Runes (amethyst crystals), rune marks in the corners; the icon in the gold-framed centre slot (10 / 13 / 16), the 8 slots around it are invisible buttons (`rune:menu_blank` model) doing the same |
| Jobs (`jobs`) | 4 | oak board with the 27 Job slots (Account Level framed gold), dark oak band with Previous / Back / Next |
| Quests (`quests`) | 3 | parchment: Reroll Tokens (4, copper), the 3 Quests (11 / 13 / 15, gold, underlined), Back (22) |
| Runes (`runes`) | 6 | deepslate band with the 8 Player Rune slots (amethyst) + Capacity (8, gold), engraved stone divider, amethyst board for the Runes in the inventory (18–44), deepslate band with Back (49) |

The v2 `gui/codex_main.png` and `gui/rune_loadout.png` are not used; `rune_loadout` stays as the idea for the future tool loadout menu.

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

**In the pack**: item definition `rune:<rune id>` (`haste`, `specialist_diamond`, `affinity_lush_caves`…) is a `range_dispatch` on `custom_model_data` `floats[0]` = the Rank; it picks model `rune:item/rune/<id>_<rank>`. Missing value = Rank I. The Max Rank (gold numeral) is baked from `RUNES` in `runes.py` (= the Job docs): if a Max Rank changes in config, change it there and re-export. The plugin sets the item model to the Rune id, and `visuals.itemrendering.RuneModelRenderer` writes the Rank into `custom_model_data` on every redraw (creation and Rank-up).

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

## Rarity icons (2026-11-23)
One 7×7 gem per Rarity, in the Rarity's name colour, in the `rune:glyphs` font (`\ue000` Common … `\ue004` Legendary; bitmap height 8, ascent 7 like vanilla text). The shape grows with Rarity: Common pebble · Uncommon rhombus · Rare cut gem · Epic crystal cluster · Legendary crown. Generator: `tools/texture-gen/v3/icons.py`. In code: `Glyphs.rarity(rarity)` (always white, or the text colour tints it) and `Text.rarityLine(rarity)` = icon + space + name, used for the Rarity line of every item and the Quest Tier.

## Tooling
- Generator: `tools/texture-gen/` (Python + Pillow, see its README). A colour change is a one-line edit; `export_all.py` rebuilds every PNG and pack file.
- Open points: real poplar palette (no poplar texture in 26.1, colours invented); some Relic loose pixels (hanging moss / roots) to check in game.
- Next batches: Blueprint, font glyphs (Rarity + Job icons), menus v3 (Jobs, Quests, Cache reel).
