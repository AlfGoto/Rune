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

## Menus (v3 free-form, 2026-11-23)
Menus are drawn as interfaces, not as chest grids (Alfred: "way better"). Every Codex menu is a 6-row chest whose whole window is drawn in its title.

**How it works** (`visuals.menu.MenuCanvas`)
- **Background**: one 176×222 glyph of the `rune:menus` font (`ascent` 13 puts its top on the window's top), drawn freely by `tools/texture-gen/v3/menus.py`. Buttons are drawn in it.
- **Clicks** still happen on the 18 px slot grid: a drawn button is covered by **invisible items** (`Button.invisible`, item model `rune:menu_blank`) carrying the tooltip and the click. Hovering shows the vanilla white square.
- **Real items** (Runes, the player's head, Quest and Job icons) sit in slots **cut out** of the background (transparent), so the item shows whatever Minecraft draws first. An empty one gets `rune:menu_empty` (a dark recess) so the grey vanilla slot never shows. Back, Reroll, Tokens are drawn, not cut out.
- **Live text and bars** are more glyphs in the title. The cursor is moved with space glyphs of `rune:menus` (``… = −1, −2 … −256 px, ``… = +1 … +256). A font row sits at one height only, so the pack builder generates one font per family and height: `rune:menu/<family>_<y>` (y 3–140 = top of the capitals in window pixels), from `menu_font.json` (written by `menufont.py`). Families: `normal` (vanilla glyphs + French / Latin accents), `shadow` (same + baked drop shadow, for dark backgrounds), `small` (3×5 capitals: numbers, long translations), `bars` (progress-bar pieces 1–16 px in green / aqua / gold / purple).
- **Translations**: text drawn in a menu is measured on the server, so it uses the server's copy of the lang files in the player's language (`MenuStrings`, French if the client is in French). Text wider than its box switches to `small`, then is cut with "..". Tooltips stay normal translatable components.
- **Live images** (fixed height, any x): Quest notes in the 5 Tier colours, the Capacity orb in 11 fill steps, page arrows. A menu showing live data rebuilds its title when the data changes (`Menu.retitle` moves the items to a new window and reopens it).
- Only menus with a fixed layout get this; `PagedMenu` (debug) stays a plain chest.

| Menu | Look | Slots |
|---|---|---|
| Rune Codex | an open book: left page = player head (gold frame), name, Level, XP bar, Capacity bar; right page = 3 chapter buttons (Jobs oak, Quests parchment, Runes amethyst) with a 16 px icon, the name and a live value (Lv 14, 2 / 3, 5 / 8) | head 11; chapters 4 × 2 from 5 / 23 / 41 |
| Runes | an altar: 8 sockets in a ring around a Capacity orb that fills up; a leather satchel with the Runes in the inventory (18 per page) | sockets 3, 5, 15, 24, 32, 30, 20, 11; orb 12–14, 21–23; satchel 36–53; Back 27, pages 26 / 35 |
| Quests | a notice board: 3 notes with a border and header in the Tier colour, icon, goal on 2 lines, progress bar, Tier; a copper reroll button under each | notes 3 × 5 from 0 / 3 / 6 (icon 10 / 13 / 16); reroll 46 / 49 / 52; Tokens 47; Back 53 |
| Jobs | Account banner with its XP bar, then one card per planned Job, 6 per page (`JobsMenu.PLANNED`, design order): a playable Job shows icon, level, XP bar, name; a Job not built yet is a grey locked card (padlock, grey icon `menu_locked_<job>`, name, tooltip "Not playable yet"); "Coming soon" only fills spots past the last Job | banner 0–8; cards 3 × 2 from 9, 12, 15, 27, 30, 33; pages 48 / 50; Back 49 |
| Cache reel | a vault: the opened Cache drawn x2 on top, a reel of 9 slots under gold pointers where Runes of random Rarities spin (fast, then slowing) and stop on the best reward, with a near miss next to it; then the frame lights up in the colour of that best Rarity and the rewards show centred, with "Rewards" under it; a sound per Rarity (firework for Legendary) | reel 18–26 (centre 22) |

The layout constants live in `menus.py` (art) and the Java menus (`CodexMenus`, `RunesMenu`, `QuestsMenu`, `JobsMenu`): change both together. Concepts and final renders: Design canvas "Rune textures — Menus and Rarity icons" (https://claude.ai/artifact/1uAJAkpFQivpaveKqcnVzN).

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
- Next batches: Blueprint, font glyphs (Job icons), menus v3 are done (Codex, Runes, Quests, Jobs, Cache reel).
