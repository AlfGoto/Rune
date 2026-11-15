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

## Tooling
- Generator: `tools/texture-gen/` (Python + Pillow, see its README). A colour change is a one-line edit; `export_all.py` rebuilds every PNG and pack file.
- Open points: real poplar palette (no poplar texture in 26.1, colours invented); some Relic loose pixels (hanging moss / roots) to check in game.
- Next batches: armor (icons + worn layers), then Rune Cache, Rune Codex, Blueprint, Rune tablets.
