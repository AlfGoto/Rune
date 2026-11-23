# texture-gen

Python scripts that draw every Rune item texture (16×16), armor icon, worn armor layer (64×32) and menu background. Art rules: `docs/art-direction.md`.

```
pip install pillow
(cd tools/texture-gen && npm install minecraft-assets)   # vanilla 26.1 textures used as templates and palettes
MC_ASSETS=/path/to/26.x/assets/minecraft/textures python3 tools/texture-gen/export_all.py
```

## v3/ — items, armor (current)
- `pix.py`: 16×16 helper (remap, tint, ASCII grids, sprite sheets). Finds vanilla textures in `node_modules/minecraft-assets` or `$MC_ASSETS_26`.
- `palettes.py`: one ramp `[dark, mid, light, highlight]` per material, sampled from a vanilla texture. A colour change is a one-line edit.
- `kit.py`: vanilla pickaxe/axe templates and the Tinkers-style parts (head, knot, grip, pommel, finial, band, double-bit) + the Rarity ladder.
- `tools.py`: declarative list of pickaxes and axes (`PICKAXES`, `AXES`), relic post-effects.
- `components.py`: the 7 shared components as ASCII grids.
- `armor.py`: 2 Jobs × 5 Rarities × 4 slots of icons on the vanilla armor templates, plus one worn-layer pair per set.
- `runes.py`: Runes as layers (stone per Rarity, logo per Rune, socket per variant, Roman Rank numeral), Legendary sparkles animation. Rules: `docs/art-direction.md`, "Runes".
- `caches.py`: Rune Caches (one per Rarity, sealed coffer) and the Rune Codex (vanilla book).
- `runes_boards.py`: Design-canvas artboards for the Runes.
- `boards.py`, `preview.py`: contact sheets for the Design canvas / quick PNG previews (not used by the export).

## v2 (legacy) — menus
- `gui.py` (+ `legacy_kit.py`, `legacy_pix.py`): chest-menu backgrounds (`codex_main`, `rune_loadout`) and the in-slot menu icons. `MC_ASSETS` (vanilla `gui/container/generic_54.png`) is only needed for the backgrounds.

`export_all.py` writes PNGs, item definitions, models and equipment definitions into `src/main/resources/pack/assets/rune/` and appends missing lines to `files.txt`. Not part of the Gradle build.
